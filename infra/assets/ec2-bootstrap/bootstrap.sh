#!/usr/bin/env bash
# deploy-app.sh → remote-app-deploy.sh から呼ばれるセットアップスクリプト。
# Docker Compose（MySQL + Backend + Frontend）とホスト Nginx を 1 台に載せます。
set -euxo pipefail

APP_ROOT="/opt/smart-household"
APP_DIR="${APP_ROOT}/app"
ENV_FILE="${APP_ROOT}/.env"
BOOTSTRAP_MARKER="${APP_ROOT}/.bootstrap-complete"

# profile=prod で本体 + AWS（ECR）オーバーレイ
COMPOSE_FILES=(
  -f docker/compose/docker-compose.yaml
  -f docker/compose/docker-compose.aws.yaml
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
      --profile prod "${COMPOSE_FILES[@]}" "$@"
  )
}

ecr_login() {
  aws ecr get-login-password --region "${AWS_REGION}" \
    | docker login --username AWS --password-stdin "${ECR_REPO_URI%%/*}"
}

install_packages() {
  log "Installing OS packages..."
  dnf update -y
  dnf install -y docker nginx git jq tar gzip unzip certbot \
    python3-certbot-nginx bind-utils
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

  local client_id issuer cors_origins app_url pool_id
  client_id="$(ssm_param "cognito/client-id")"
  issuer="$(ssm_param "cognito/issuer-url")"
  cors_origins="$(ssm_param "domain/cors-allowed-origins")"
  app_url="$(ssm_param "domain/app-url")"
  pool_id="$(ssm_param "cognito/user-pool-id")"

  # Frontend ECR URI（deploy-app が渡す）。未設定時は Backend と同じレジストリ配下の frontend を推定しないよう必須にする。
  if [[ -z "${ECR_FRONTEND_REPO_URI:-}" ]]; then
    log "ERROR: ECR_FRONTEND_REPO_URI が未設定です。deploy-app.sh を新しい版で実行してください。"
    exit 1
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
SPRING_PROFILES_ACTIVE=prod
COGNITO_ISSUER_URL=${issuer}
COGNITO_CLIENT_ID=${client_id}
OPENAI_API_KEY=${openai_key}
OPENAI_API_URL=${openai_url}
CORS_ALLOWED_ORIGINS=${cors_origins}
ECR_BACKEND_IMAGE=${ECR_REPO_URI}:latest
ECR_FRONTEND_IMAGE=${ECR_FRONTEND_REPO_URI}:latest
NEXT_PUBLIC_API_BASE_URL=${app_url}
NEXT_PUBLIC_AWS_REGION=${AWS_REGION}
NEXT_PUBLIC_COGNITO_USER_POOL_ID=${pool_id}
NEXT_PUBLIC_COGNITO_CLIENT_ID=${client_id}
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
    log "ERROR: domain/name が未設定です。cdk.context.json の domainName を設定して deploy.sh を再実行してください。"
    exit 1
  fi

  if [[ -z "${certbot_email}" || "${certbot_email}" == "None" ]]; then
    log "ERROR: domain/certbot-email が未設定です。cdk.context.json の certbotEmail を設定して deploy.sh を再実行してください。"
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

# --- 特殊モード（remote-app-deploy.sh から呼び出し） ---

if [[ "${BOOTSTRAP_MODE:-}" == "update-app" ]]; then
  write_env_file
  ecr_login
  compose pull backend frontend
  compose up -d
  log "App updated (.env refreshed, images pulled, stack restarted)."
  exit 0
fi

# --- 通常モード（初回フル bootstrap） ---

install_packages
fetch_application_source
write_env_file
configure_nginx
setup_https

ecr_login
if docker pull "${ECR_REPO_URI}:latest" && docker pull "${ECR_FRONTEND_REPO_URI}:latest"; then
  compose up -d
else
  log "Backend/Frontend image not found in ECR yet. Run ./infra/scripts/deploy-app.sh after deploy."
fi

touch "${BOOTSTRAP_MARKER}"
log "Bootstrap completed."
