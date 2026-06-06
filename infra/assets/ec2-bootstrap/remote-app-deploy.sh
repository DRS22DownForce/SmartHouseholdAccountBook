#!/usr/bin/env bash
# deploy-app.sh から SSM 経由で実行。EC2 上で bootstrap 修復 + Backend/Frontend 更新。
set -euxo pipefail

APP_ROOT="/opt/smart-household"
ENV_FILE="${APP_ROOT}/.env"
APP_DIR="${APP_ROOT}/app"
FRONTEND_MARKER="${APP_ROOT}/.frontend-complete"

log() {
  echo "[remote-app-deploy] $*"
}

ensure_bootstrap_files() {
  if [[ -x "${APP_ROOT}/bootstrap/bootstrap.sh" ]]; then
    return 0
  fi
  if [[ -z "${BOOTSTRAP_ASSET_URL:-}" ]]; then
    log "ERROR: BOOTSTRAP_ASSET_URL is not set"
    exit 1
  fi
  log "Downloading bootstrap asset from S3..."
  dnf install -y aws-cli unzip
  mkdir -p "${APP_ROOT}/bootstrap"
  aws s3 cp "${BOOTSTRAP_ASSET_URL}" /tmp/bootstrap.zip --region "${AWS_REGION}"
  unzip -o /tmp/bootstrap.zip -d "${APP_ROOT}/bootstrap"
  chmod +x "${APP_ROOT}/bootstrap/bootstrap.sh" "${APP_ROOT}/bootstrap/remote-app-deploy.sh"
}

needs_full_bootstrap() {
  if [[ ! -f "${APP_ROOT}/.bootstrap-complete" ]]; then
    return 0
  fi
  if ! command -v docker >/dev/null 2>&1; then
    return 0
  fi
  if [[ ! -f "${ENV_FILE}" ]]; then
    return 0
  fi
  if [[ ! -f "${APP_DIR}/docker/compose/docker-compose.single-host.aws.yaml" ]]; then
    return 0
  fi
  return 1
}

needs_frontend_setup() {
  if [[ ! -f "${APP_DIR}/frontend-nextjs/package.json" ]]; then
    return 1
  fi
  if [[ -f "${FRONTEND_MARKER}" ]] && systemctl is-active --quiet smart-household-frontend.service 2>/dev/null; then
    return 1
  fi
  return 0
}

run_full_bootstrap() {
  log "Running full bootstrap..."
  export PROJECT_NAME AWS_REGION ECR_REPO_URI APP_SECRET_ARN
  "${APP_ROOT}/bootstrap/bootstrap.sh"
}

run_frontend_setup() {
  log "Building and starting frontend (Next.js)..."
  export PROJECT_NAME AWS_REGION ECR_REPO_URI APP_SECRET_ARN
  BOOTSTRAP_MODE=frontend-only "${APP_ROOT}/bootstrap/bootstrap.sh"
}

refresh_runtime_env() {
  log "Refreshing .env from Secrets Manager..."
  export PROJECT_NAME AWS_REGION ECR_REPO_URI APP_SECRET_ARN
  BOOTSTRAP_MODE=refresh-env-only "${APP_ROOT}/bootstrap/bootstrap.sh"
}

restart_backend_only() {
  refresh_runtime_env
  log "Pulling and restarting backend container..."
  aws ecr get-login-password --region "${AWS_REGION}" \
    | docker login --username AWS --password-stdin "${ECR_REPO_URI%%/*}"

  cd "${APP_DIR}"
  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    pull backend

  docker compose --project-directory "${APP_DIR}" --env-file "${ENV_FILE}" \
    -f docker/compose/docker-compose.single-host.yaml \
    -f docker/compose/docker-compose.single-host.prod.yaml \
    -f docker/compose/docker-compose.single-host.aws.yaml \
    up -d
}

ensure_bootstrap_files

if needs_full_bootstrap; then
  run_full_bootstrap
else
  restart_backend_only
  if needs_frontend_setup; then
    run_frontend_setup
  fi
fi

log "Done."
