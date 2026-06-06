#!/usr/bin/env bash
# EC2 初回起動時に User Data から呼ばれるセットアップスクリプト。
# Docker Compose（MySQL + Backend）と Nginx + Next.js を 1 台に載せます。
set -euxo pipefail

APP_ROOT="/opt/smart-household"
APP_DIR="${APP_ROOT}/app"
ENV_FILE="${APP_ROOT}/.env"

log() {
  echo "[bootstrap] $*"
}

# EC2 メタデータは IMDSv2 必須の環境があるため、トークン付きで取得する
ec2_metadata() {
  local path="$1"
  local token
  token="$(curl -fsS -X PUT "http://169.254.169.254/latest/api/token" \
    -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")"
  curl -fsS "http://169.254.169.254${path}" \
    -H "X-aws-ec2-metadata-token: ${token}"
}

ec2_public_ipv4() {
  local ip=""
  ip="$(ec2_metadata "/latest/meta-data/public-ipv4" 2>/dev/null || true)"
  if [[ -z "${ip}" ]]; then
    local instance_id
    instance_id="$(ec2_metadata "/latest/meta-data/instance-id" 2>/dev/null || true)"
    if [[ -n "${instance_id}" ]]; then
      ip="$(aws ec2 describe-instances \
        --instance-ids "${instance_id}" \
        --query 'Reservations[0].Instances[0].PublicIpAddress' \
        --output text \
        --region "${AWS_REGION}" 2>/dev/null || true)"
    fi
  fi
  echo "${ip}"
}

BOOTSTRAP_MARKER="${APP_ROOT}/.bootstrap-complete"
FRONTEND_MARKER="${APP_ROOT}/.frontend-complete"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    log "ERROR: required command not found: $1"
    exit 1
  }
}

install_packages() {
  log "Installing OS packages..."
  dnf update -y
  dnf install -y docker nginx git jq tar gzip unzip certbot python3-certbot-nginx bind-utils java-21-amazon-corretto-headless
  systemctl enable --now docker
  systemctl enable nginx

  # t4g.small でも Next.js ビルドは RAM を多く使うため、ビルド中は swap も確保
  local want_swap_mb=4096
  local current_swap_mb
  current_swap_mb="$(free -m | awk '/^Swap:/ {print $2}')"
  if [[ "${current_swap_mb}" -lt "${want_swap_mb}" ]]; then
    local add_mb=$((want_swap_mb - current_swap_mb))
    log "Adding ${add_mb}MB swap (current: ${current_swap_mb}MB)..."
    local swap_path="/swapfile$((current_swap_mb > 0 ? 2 : 1))"
    fallocate -l "${add_mb}M" "${swap_path}" 2>/dev/null \
      || dd if=/dev/zero of="${swap_path}" bs=1M count="${add_mb}" status=none
    chmod 600 "${swap_path}"
    mkswap "${swap_path}"
    swapon "${swap_path}"
    grep -q "${swap_path}" /etc/fstab || echo "${swap_path} none swap sw 0 0" >> /etc/fstab
  fi

  if ! docker compose version >/dev/null 2>&1; then
    log "Installing Docker Compose plugin..."
    mkdir -p /usr/local/lib/docker/cli-plugins
    ARCH="$(uname -m)"
    COMPOSE_ARCH="aarch64"
    if [[ "${ARCH}" == "x86_64" ]]; then
      COMPOSE_ARCH="x86_64"
    fi
    curl -fsSL "https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-${COMPOSE_ARCH}" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
  fi

  if ! command -v node >/dev/null 2>&1; then
    log "Installing Node.js 20..."
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
    dnf install -y nodejs
  fi
}

fetch_application_source() {
  mkdir -p "${APP_DIR}"
  local git_url
  local git_branch
  git_url="$(aws ssm get-parameter --name "/${PROJECT_NAME}/deploy/git-repository-url" --query Parameter.Value --output text --region "${AWS_REGION}")"
  git_branch="$(aws ssm get-parameter --name "/${PROJECT_NAME}/deploy/git-repository-branch" --query Parameter.Value --output text --region "${AWS_REGION}")"

  if [[ -n "${git_url}" && "${git_url}" != "None" && "${git_url}" != "null" && "${git_url}" != "none" ]]; then
    log "Cloning repository: ${git_url} (${git_branch})"
    if [[ -d "${APP_DIR}/.git" ]]; then
      git -C "${APP_DIR}" fetch --depth 1 origin "${git_branch}"
      git -C "${APP_DIR}" checkout "${git_branch}"
      git -C "${APP_DIR}" reset --hard "origin/${git_branch}"
    else
      rm -rf "${APP_DIR}"
      if ! git clone --depth 1 --branch "${git_branch}" "${git_url}" "${APP_DIR}" 2>/dev/null; then
        log "Branch '${git_branch}' not found. Cloning default branch instead."
        rm -rf "${APP_DIR}"
        git clone --depth 1 "${git_url}" "${APP_DIR}"
      fi
    fi
    apply_bundled_docker_overlays
  else
    log "gitRepositoryUrl is empty. Using bundled docker files only."
    mkdir -p "${APP_DIR}/docker/compose" "${APP_DIR}/docker/mysql/init" "${APP_DIR}/docker/mysql"
    cp -a "${APP_ROOT}/bootstrap/bundled/docker/." "${APP_DIR}/docker/"
  fi
}

# Git リポジトリに AWS 用 compose が無い場合でも EC2 デプロイできるよう、bundle を上書き適用
apply_bundled_docker_overlays() {
  log "Applying AWS docker overlays from bootstrap bundle..."
  mkdir -p "${APP_DIR}/docker/compose" "${APP_DIR}/docker/mysql/init" "${APP_DIR}/docker/mysql"
  cp -a "${APP_ROOT}/bootstrap/bundled/docker/." "${APP_DIR}/docker/"
}

load_secret_json() {
  aws secretsmanager get-secret-value \
    --secret-id "${APP_SECRET_ARN}" \
    --query SecretString \
    --output text \
    --region "${AWS_REGION}"
}

write_env_file() {
  log "Writing ${ENV_FILE}"
  local secret_json retries=0 max_retries=40
  # init-secrets.sh が deploy 後に実行される場合に備え、Secrets が入るまで待つ
  while [[ "${retries}" -lt "${max_retries}" ]]; do
    secret_json="$(load_secret_json 2>/dev/null || true)"
    local mysql_root_check
    mysql_root_check="$(echo "${secret_json}" | jq -r '.MYSQL_ROOT_PASSWORD // empty' 2>/dev/null || true)"
    if [[ -n "${mysql_root_check}" ]]; then
      break
    fi
    log "Waiting for Secrets Manager (${retries}/${max_retries})..."
    sleep 15
    retries=$((retries + 1))
  done

  local mysql_root mysql_flyway mysql_app mysql_db openai_key openai_url cors_origins
  mysql_root="$(echo "${secret_json}" | jq -r '.MYSQL_ROOT_PASSWORD // empty')"
  mysql_flyway="$(echo "${secret_json}" | jq -r '.MYSQL_FLYWAY_PASSWORD // empty')"
  mysql_app="$(echo "${secret_json}" | jq -r '.MYSQL_APP_PASSWORD // empty')"
  mysql_db="$(echo "${secret_json}" | jq -r '.MYSQL_DATABASE // "household_book"')"
  openai_key="$(echo "${secret_json}" | jq -r '.OPENAI_API_KEY // empty')"
  openai_url="$(echo "${secret_json}" | jq -r '.OPENAI_API_URL // "https://api.openai.com/v1/chat/completions"')"
  # 旧デプロイの誤った URL を補正（OpenAI Chat Completions エンドポイント）
  if [[ "${openai_url}" == "https://api.openai.com/v1" ]]; then
    openai_url="https://api.openai.com/v1/chat/completions"
  fi
  cors_origins="$(echo "${secret_json}" | jq -r '.CORS_ALLOWED_ORIGINS // empty')"

  if [[ -z "${mysql_root}" || -z "${mysql_flyway}" || -z "${mysql_app}" ]]; then
    log "ERROR: Secrets Manager に DB パスワードがありません。"
    log "       デプロイ前に ./infra/scripts/init-secrets.sh を実行してください。"
    exit 1
  fi

  local pool_id client_id issuer jwk
  pool_id="$(aws ssm get-parameter --name "/${PROJECT_NAME}/cognito/user-pool-id" --query Parameter.Value --output text --region "${AWS_REGION}")"
  client_id="$(aws ssm get-parameter --name "/${PROJECT_NAME}/cognito/client-id" --query Parameter.Value --output text --region "${AWS_REGION}")"
  issuer="$(aws ssm get-parameter --name "/${PROJECT_NAME}/cognito/issuer-url" --query Parameter.Value --output text --region "${AWS_REGION}")"
  jwk="${issuer}/.well-known/jwks.json"

  if [[ -z "${cors_origins}" || "${cors_origins}" == "null" ]]; then
    cors_origins="$(aws ssm get-parameter --name "/${PROJECT_NAME}/domain/app-url" --query Parameter.Value --output text --region "${AWS_REGION}" 2>/dev/null || true)"
    if [[ -z "${cors_origins}" || "${cors_origins}" == "None" ]]; then
      cors_origins="http://$(ec2_public_ipv4)"
    fi
  fi

  cat > "${ENV_FILE}" <<EOF
MYSQL_ROOT_PASSWORD=${mysql_root}
MYSQL_DATABASE=${mysql_db}
MYSQL_FLYWAY_USER=flyway_user
MYSQL_FLYWAY_PASSWORD=${mysql_flyway}
MYSQL_APP_USER=app_user
MYSQL_APP_PASSWORD=${mysql_app}
SPRING_DATASOURCE_URL_PROD=jdbc:mysql://mysql:3306/${mysql_db}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
SPRING_DATASOURCE_URL_DEV=jdbc:mysql://localhost:3306/${mysql_db}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
COGNITO_JWK_SET_URL=${jwk}
COGNITO_ISSUER_URL=${issuer}
COGNITO_CLIENT_ID=${client_id}
OPENAI_API_KEY=${openai_key}
OPENAI_API_URL=${openai_url}
CORS_ALLOWED_ORIGINS=${cors_origins}
ECR_BACKEND_IMAGE=${ECR_REPO_URI}:latest
EOF
}

configure_nginx() {
  log "Configuring Nginx..."
  local domain_name
  domain_name="$(aws ssm get-parameter --name "/${PROJECT_NAME}/domain/name" --query Parameter.Value --output text --region "${AWS_REGION}")"
  sed "s/DOMAIN_NAME_PLACEHOLDER/${domain_name}/g" \
    "${APP_ROOT}/bootstrap/nginx/smart-household.conf" > /etc/nginx/conf.d/smart-household.conf
  rm -f /etc/nginx/conf.d/default.conf || true
  nginx -t
  systemctl restart nginx
}

setup_https() {
  local domain_name certbot_email
  domain_name="$(aws ssm get-parameter --name "/${PROJECT_NAME}/domain/name" --query Parameter.Value --output text --region "${AWS_REGION}")"
  certbot_email="$(aws ssm get-parameter --name "/${PROJECT_NAME}/domain/certbot-email" --query Parameter.Value --output text --region "${AWS_REGION}")"

  if [[ -z "${domain_name}" || "${domain_name}" == "None" ]]; then
    log "Domain not configured. Skip HTTPS."
    return 0
  fi

  log "Waiting for DNS ${domain_name} to resolve to this host..."
  local expected_ip my_ip retries=0
  expected_ip="$(ec2_public_ipv4)"
  if [[ -z "${expected_ip}" || "${expected_ip}" == "None" ]]; then
    log "WARN: Could not detect public IP. Skipping DNS wait."
  else
    until [[ "${retries}" -ge 30 ]]; do
      my_ip="$(dig +short "${domain_name}" | tail -n1 || true)"
      if [[ "${my_ip}" == "${expected_ip}" ]]; then
        break
      fi
      retries=$((retries + 1))
      sleep 10
    done

    if [[ "${my_ip}" != "${expected_ip}" ]]; then
      log "WARN: DNS not yet pointing to ${expected_ip} (got: ${my_ip}). certbot may fail."
    fi
  fi

  log "Obtaining Let's Encrypt certificate via certbot..."
  local certbot_domains=(-d "${domain_name}")
  # ルートドメイン（example.com 形式）なら www も HTTPS 対象
  local label_count
  label_count="$(echo "${domain_name}" | awk -F. '{print NF}')"
  if [[ "${label_count}" -eq 2 ]]; then
    certbot_domains+=(-d "www.${domain_name}")
  fi

  certbot --nginx \
    "${certbot_domains[@]}" \
    --non-interactive \
    --agree-tos \
    -m "${certbot_email}" \
    --redirect

  systemctl enable certbot-renew.timer || true
  systemctl start certbot-renew.timer || true
}

ecr_login_and_compose_up() {
  log "Starting Docker Compose stack..."
  aws ecr get-login-password --region "${AWS_REGION}" \
    | docker login --username AWS --password-stdin "${ECR_REPO_URI%%/*}"

  cd "${APP_DIR}"
  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    up -d
}

setup_frontend_unit() {
  if [[ ! -f "${APP_DIR}/frontend-nextjs/package.json" ]]; then
    log "Frontend source not found. Skip Next.js setup."
    return 0
  fi

  log "Building frontend (this may take several minutes on first boot)..."
  local app_url
  app_url="$(aws ssm get-parameter --name "/${PROJECT_NAME}/domain/app-url" --query Parameter.Value --output text --region "${AWS_REGION}")"
  cat > "${APP_DIR}/frontend-nextjs/.env.local" <<EOF
NEXT_PUBLIC_API_BASE_URL=${app_url}
NEXT_PUBLIC_AWS_REGION=${AWS_REGION}
NEXT_PUBLIC_COGNITO_USER_POOL_ID=$(aws ssm get-parameter --name "/${PROJECT_NAME}/cognito/user-pool-id" --query Parameter.Value --output text --region "${AWS_REGION}")
NEXT_PUBLIC_COGNITO_CLIENT_ID=$(aws ssm get-parameter --name "/${PROJECT_NAME}/cognito/client-id" --query Parameter.Value --output text --region "${AWS_REGION}")
EOF

  # MySQL + Backend 起動中は RAM が足りないため、ビルド中だけ Compose を止める
  log "Stopping Docker stack temporarily to free RAM for frontend build..."
  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    stop || true

  cd "${APP_DIR}/frontend-nextjs"
  export NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=1536}"
  export NEXT_TELEMETRY_DISABLED=1
  npm ci
  if ! command -v java >/dev/null 2>&1; then
    log "Installing Java for OpenAPI code generation..."
    dnf install -y java-21-amazon-corretto-headless
  fi
  log "Generating TypeScript API client from OpenAPI spec..."
  npm run generate:api
  npm run build

  log "Restarting Docker stack after frontend build..."
  cd "${APP_DIR}"
  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    up -d

  cat > /etc/systemd/system/smart-household-frontend.service <<'UNIT'
[Unit]
Description=Smart Household Next.js frontend
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/smart-household/app/frontend-nextjs
Environment=NODE_ENV=production
Environment=PORT=3000
ExecStart=/usr/bin/npm run start
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT

  systemctl daemon-reload
  systemctl enable --now smart-household-frontend.service
}

# deploy-app から .env を Secrets Manager の最新値で更新し Backend を再起動
if [[ "${BOOTSTRAP_MODE:-}" == "refresh-env-only" ]]; then
  write_env_file
  cd "${APP_DIR}"
  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    up -d
  log "Runtime .env refreshed and Docker stack restarted."
  exit 0
fi

# deploy-app からフロントのみ再ビルドするとき（インスタンスサイズ変更後など）
if [[ "${BOOTSTRAP_MODE:-}" == "frontend-only" ]]; then
  if [[ ! -f "${ENV_FILE}" ]]; then
    log "ERROR: ${ENV_FILE} がありません。先に full bootstrap を実行してください。"
    exit 1
  fi
  setup_frontend_unit
  touch "${FRONTEND_MARKER}"
  log "Frontend setup completed."
  exit 0
fi

install_packages
fetch_application_source
write_env_file
configure_nginx
setup_https

# 初回は ECR ログイン後に pull → Compose 起動（Frontend ビルドより先に Backend を立ち上げる）
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_REPO_URI%%/*}"

if docker pull "${ECR_REPO_URI}:latest"; then
  ecr_login_and_compose_up
else
  log "Backend image not found in ECR yet. Run ./infra/scripts/deploy-app.sh after deploy."
fi

setup_frontend_unit
touch "${BOOTSTRAP_MARKER}"
touch "${FRONTEND_MARKER}"
log "Bootstrap completed."
