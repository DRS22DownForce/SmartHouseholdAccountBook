#!/usr/bin/env bash
# deploy-app.sh → remote-app-deploy.sh から呼ばれるセットアップスクリプト。
# Docker Compose（MySQL + Backend）と Nginx + Next.js を 1 台に載せます。
set -euxo pipefail

APP_ROOT="/opt/smart-household"
APP_DIR="${APP_ROOT}/app"
ENV_FILE="${APP_ROOT}/.env"
BOOTSTRAP_MARKER="${APP_ROOT}/.bootstrap-complete"
FRONTEND_MARKER="${APP_ROOT}/.frontend-complete"

COMPOSE_FILES=(
  -f docker/compose/docker-compose.single-host.yaml
  -f docker/compose/docker-compose.single-host.prod.yaml
  -f docker/compose/docker-compose.single-host.aws.yaml
)

log() {
  echo "[bootstrap] $*"
}

# SSM Parameter Store から値を取得（bootstrap 中は CDK が必ず書き込む前提）
ssm_param() {
  aws ssm get-parameter \
    --name "/${PROJECT_NAME}/$1" \
    --query Parameter.Value \
    --output text \
    --region "${AWS_REGION}"
}

# docker compose の -f 相対パスは CWD 基準で解決される（--project-directory ではない）。
# SSM 経由実行時は CWD が / になり得るため、必ず APP_DIR に移動してから実行する。
compose() {
  (
    cd "${APP_DIR}"
    docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
      "${COMPOSE_FILES[@]}" "$@"
  )
}

ecr_login() {
  aws ecr get-login-password --region "${AWS_REGION}" \
    | docker login --username AWS --password-stdin "${ECR_REPO_URI%%/*}"
}

# t4g.small 向け: Next.js ビルド用 swap（初回のみ作成。再実行時は既存を触らない）
ensure_swap() {
  local swap_path="/swapfile"
  local want_swap_mb=4096

  [[ -f "${swap_path}" ]] && return 0
  [[ "$(free -m | awk '/^Swap:/ {print $2}')" -ge $((want_swap_mb * 95 / 100)) ]] && return 0

  log "Creating ${want_swap_mb}MB swap at ${swap_path}..."
  fallocate -l "${want_swap_mb}M" "${swap_path}"
  chmod 600 "${swap_path}"
  mkswap "${swap_path}"
  swapon "${swap_path}"
  grep -q "${swap_path}" /etc/fstab || echo "${swap_path} none swap sw 0 0" >> /etc/fstab
}

install_packages() {
  log "Installing OS packages..."
  dnf update -y
  dnf install -y docker nginx git jq tar gzip unzip certbot \
    python3-certbot-nginx bind-utils java-21-amazon-corretto-headless
  systemctl enable --now docker
  systemctl enable nginx

  # AL2023 標準リポジトリに compose プラグインが無いため手動配置
  if ! docker compose version >/dev/null 2>&1; then
    local compose_plugin=/usr/libexec/docker/cli-plugins/docker-compose
    mkdir -p "$(dirname "${compose_plugin}")"
    curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
      -o "${compose_plugin}"
    chmod +x "${compose_plugin}"
  fi

  if ! command -v node >/dev/null 2>&1; then
    log "Installing Node.js 20..."
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
    dnf install -y nodejs
  fi

  ensure_swap
}

fetch_application_source() {
  local git_url git_branch
  git_url="$(ssm_param "deploy/git-repository-url")"
  git_branch="$(ssm_param "deploy/git-repository-branch")"

  if [[ "${git_url}" != "none" && -n "${git_url}" ]]; then
    log "Cloning repository: ${git_url} (${git_branch})"
    rm -rf "${APP_DIR}"
    git clone --depth 1 --branch "${git_branch}" "${git_url}" "${APP_DIR}"
  else
    log "gitRepositoryUrl is none. Using bundled docker files only."
    mkdir -p "${APP_DIR}/docker"
  fi

  log "Applying AWS docker overlays from bootstrap bundle..."
  mkdir -p "${APP_DIR}/docker/compose" "${APP_DIR}/docker/mysql/init" "${APP_DIR}/docker/mysql"
  cp -a "${APP_ROOT}/bootstrap/bundled/docker/." "${APP_DIR}/docker/"
}

write_env_file() {
  log "Writing ${ENV_FILE}"
  local secret_json retries=0 max_retries=20

  # init-secrets.sh 実行前に bootstrap が走る場合に備えて待機
  while [[ "${retries}" -lt "${max_retries}" ]]; do
    secret_json="$(aws secretsmanager get-secret-value \
      --secret-id "${APP_SECRET_ARN}" \
      --query SecretString \
      --output text \
      --region "${AWS_REGION}" 2>/dev/null || true)"
    if [[ -n "$(echo "${secret_json}" | jq -r '.MYSQL_ROOT_PASSWORD // empty')" ]]; then
      break
    fi
    log "Waiting for Secrets Manager (${retries}/${max_retries})..."
    sleep 15
    retries=$((retries + 1))
  done

  local mysql_root mysql_flyway mysql_app mysql_db openai_key openai_url
  mysql_root="$(echo "${secret_json}" | jq -r '.MYSQL_ROOT_PASSWORD // empty')"
  mysql_flyway="$(echo "${secret_json}" | jq -r '.MYSQL_FLYWAY_PASSWORD // empty')"
  mysql_app="$(echo "${secret_json}" | jq -r '.MYSQL_APP_PASSWORD // empty')"
  mysql_db="$(echo "${secret_json}" | jq -r '.MYSQL_DATABASE // "household_book"')"
  openai_key="$(echo "${secret_json}" | jq -r '.OPENAI_API_KEY // empty')"
  openai_url="$(echo "${secret_json}" | jq -r '.OPENAI_API_URL // "https://api.openai.com/v1/chat/completions"')"

  if [[ -z "${mysql_root}" || -z "${mysql_flyway}" || -z "${mysql_app}" ]]; then
    log "ERROR: Secrets Manager に DB パスワードがありません。"
    log "       デプロイ前に ./infra/scripts/init-secrets.sh を実行してください。"
    exit 1
  fi

  local client_id issuer jwk cors_origins
  client_id="$(ssm_param "cognito/client-id")"
  issuer="$(ssm_param "cognito/issuer-url")"
  jwk="${issuer}/.well-known/jwks.json"
  cors_origins="$(ssm_param "domain/cors-allowed-origins")"

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
  domain_name="$(ssm_param "domain/name")"
  sed "s/DOMAIN_NAME_PLACEHOLDER/${domain_name}/g" \
    "${APP_ROOT}/bootstrap/nginx/smart-household.conf" > /etc/nginx/conf.d/smart-household.conf
  rm -f /etc/nginx/conf.d/default.conf || true
  nginx -t
  systemctl restart nginx
}

# IMDSv2 トークン取得（EC2 メタデータ API 用）
imds_token() {
  curl -sf -X PUT "http://169.254.169.254/latest/api/token" \
    -H "X-aws-ec2-metadata-token-ttl-seconds: 60"
}

# この EC2 に割り当てられたパブリック IP（Elastic IP）を取得
instance_public_ip() {
  local token
  token="$(imds_token)"
  curl -sf -H "X-aws-ec2-metadata-token: ${token}" \
    "http://169.254.169.254/latest/meta-data/public-ipv4"
}

# 指定ドメインの A レコードがこの EC2 を向くまで待機
wait_for_dns() {
  local domain="$1"
  local expected_ip="$2"
  local retries=0
  local max_retries=20
  local interval_sec=15

  while [[ "${retries}" -lt "${max_retries}" ]]; do
    local resolved
    resolved="$(dig +short "${domain}" A 2>/dev/null | head -n1)"

    if [[ "${resolved}" == "${expected_ip}" ]]; then
      log "DNS OK: ${domain} -> ${resolved}"
      return 0
    fi

    log "Waiting for DNS (${domain}: ${resolved:-none} -> want ${expected_ip}) [${retries}/${max_retries}]..."
    sleep "${interval_sec}"
    retries=$((retries + 1))
  done

  log "ERROR: ${domain} がこの EC2 (${expected_ip}) を向いていません。"
  log "       Route 53 の A レコードと DNS 伝播を確認してください。"
  return 1
}

setup_https() {
  local domain_name certbot_email
  domain_name="$(ssm_param "domain/name")"
  certbot_email="$(ssm_param "domain/certbot-email")"

  if [[ -z "${domain_name}" || "${domain_name}" == "None" ]]; then
    log "ERROR: domain/name が未設定です。cdk.local.json の domainName を設定して deploy.sh を再実行してください。"
    exit 1
  fi

  if [[ -z "${certbot_email}" || "${certbot_email}" == "None" ]]; then
    log "ERROR: domain/certbot-email が未設定です。cdk.local.json の certbotEmail を設定して deploy.sh を再実行してください。"
    exit 1
  fi

  local expected_ip
  expected_ip="$(instance_public_ip)" || {
    log "ERROR: EC2 のパブリック IP を取得できませんでした。"
    exit 1
  }

  wait_for_dns "${domain_name}" "${expected_ip}" || exit 1

  log "Obtaining Let's Encrypt certificate via certbot..."
  if ! certbot --nginx \
      -d "${domain_name}" \
      --non-interactive \
      --agree-tos \
      -m "${certbot_email}" \
      --redirect; then
    log "ERROR: certbot が失敗しました。DNS・ポート80・メールアドレスを確認し、手動で再実行してください:"
    log "       certbot --nginx -d ${domain_name} --non-interactive --agree-tos -m ${certbot_email} --redirect"
    exit 1
  fi

  systemctl enable certbot-renew.timer || true
  systemctl start certbot-renew.timer || true
  log "HTTPS setup completed."
}

setup_frontend_unit() {
  if [[ ! -f "${APP_DIR}/frontend-nextjs/package.json" ]]; then
    log "Frontend source not found. Skip Next.js setup."
    return 0
  fi

  log "Building frontend (this may take several minutes on first boot)..."
  local app_url pool_id client_id
  app_url="$(ssm_param "domain/app-url")"
  pool_id="$(ssm_param "cognito/user-pool-id")"
  client_id="$(ssm_param "cognito/client-id")"

  cat > "${APP_DIR}/frontend-nextjs/.env.local" <<EOF
NEXT_PUBLIC_API_BASE_URL=${app_url}
NEXT_PUBLIC_AWS_REGION=${AWS_REGION}
NEXT_PUBLIC_COGNITO_USER_POOL_ID=${pool_id}
NEXT_PUBLIC_COGNITO_CLIENT_ID=${client_id}
EOF

  # ビルド中は RAM 確保のため Compose を一時停止
  log "Stopping Docker stack temporarily to free RAM for frontend build..."
  compose stop || true

  cd "${APP_DIR}/frontend-nextjs"
  export NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=1536}"
  export NEXT_TELEMETRY_DISABLED=1
  npm ci
  npm run generate:api
  npm run build

  log "Restarting Docker stack after frontend build..."
  cd "${APP_DIR}"
  compose up -d

#Next.jsサーバーをsystemdのサービスとして登録する
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

# --- 特殊モード（remote-app-deploy.sh から呼び出し） ---

if [[ "${BOOTSTRAP_MODE:-}" == "update-backend" ]]; then
  write_env_file
  ecr_login
  compose pull backend
  compose up -d
  log "Backend updated (.env refreshed, image pulled, stack restarted)."
  exit 0
fi

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

# --- 通常モード（初回フル bootstrap） ---

install_packages
fetch_application_source
write_env_file
configure_nginx
setup_https

ecr_login
if docker pull "${ECR_REPO_URI}:latest"; then
  compose up -d
else
  log "Backend image not found in ECR yet. Run ./infra/scripts/deploy-app.sh after deploy."
fi

setup_frontend_unit
touch "${BOOTSTRAP_MARKER}" "${FRONTEND_MARKER}"
log "Bootstrap completed."
