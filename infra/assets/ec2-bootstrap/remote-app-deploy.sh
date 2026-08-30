#!/usr/bin/env bash
# deploy-app.sh から SSM 経由で実行する唯一のエントリポイント。
# bootstrap zip は deploy-app.sh が展開済み。初回・更新ともここから bootstrap.sh を呼ぶ。
set -euxo pipefail

exec >> /var/log/smart-household-bootstrap.log 2>&1

APP_ROOT="/opt/smart-household"
BOOTSTRAP="${APP_ROOT}/bootstrap/bootstrap.sh"

log() {
  echo "[remote-app-deploy] $*"
}

export PROJECT_NAME AWS_REGION ECR_REPO_URI ECR_FRONTEND_REPO_URI APP_SECRET_ARN

if [[ ! -f "${APP_ROOT}/.bootstrap-complete" ]]; then
  log "First-time setup: running full bootstrap..."
  "${BOOTSTRAP}"
else
  log "Updating app (backend + frontend images)..."
  BOOTSTRAP_MODE=update-app "${BOOTSTRAP}"
fi

log "Done."
