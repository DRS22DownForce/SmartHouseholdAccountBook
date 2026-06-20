#!/usr/bin/env bash
# deploy-app.sh から SSM 経由で実行する唯一のエントリポイント。
# bootstrap zip は deploy-app.sh が展開済み。初回・更新ともここから bootstrap.sh を呼ぶ。
set -euxo pipefail

exec >> /var/log/smart-household-bootstrap.log 2>&1

APP_ROOT="/opt/smart-household"
BOOTSTRAP="${APP_ROOT}/bootstrap/bootstrap.sh"
APP_DIR="${APP_ROOT}/app"
FRONTEND_MARKER="${APP_ROOT}/.frontend-complete"

log() {
  echo "[remote-app-deploy] $*"
}

# Frontend が未ビルド、または systemd サービスが停止している場合に true
needs_frontend_setup() {
  if [[ ! -f "${APP_DIR}/frontend-nextjs/package.json" ]]; then
    return 1
  fi
  if [[ -f "${FRONTEND_MARKER}" ]] \
     && systemctl is-active --quiet smart-household-frontend.service 2>/dev/null; then
    return 1
  fi
  return 0
}

export PROJECT_NAME AWS_REGION ECR_REPO_URI APP_SECRET_ARN

if [[ ! -f "${APP_ROOT}/.bootstrap-complete" ]]; then
  log "First-time setup: running full bootstrap..."
  "${BOOTSTRAP}"
else
  log "Updating backend..."
  BOOTSTRAP_MODE=update-backend "${BOOTSTRAP}" #環境変数BOOTSTRAP_MODEにupdate-backendを設定してbootstrap.shを実行する

  if needs_frontend_setup; then
    log "Building and starting frontend (Next.js)..."
    BOOTSTRAP_MODE=frontend-only "${BOOTSTRAP}"
  fi
fi

log "Done."
