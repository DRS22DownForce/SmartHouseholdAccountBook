#!/usr/bin/env bash
# Secrets Manager にアプリ用シークレットを投入する（初回デプロイ前に 1 回実行）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
INFRA_DIR="${ROOT}/infra"
PROJECT_NAME="${PROJECT_NAME:-smart-household}"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"
SECRET_NAME="${PROJECT_NAME}/app"

OPENAI_API_URL="${OPENAI_API_URL:-https://api.openai.com/v1/chat/completions}"

read -r -p "OpenAI API Key (空 Enter でプレースホルダ): " OPENAI_API_KEY
OPENAI_API_KEY="${OPENAI_API_KEY:-REPLACE_ME}"

MYSQL_ROOT_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=' | head -c 32)"
MYSQL_FLYWAY_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=' | head -c 32)"
MYSQL_APP_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=' | head -c 32)"

SECRET_JSON="$(python3 <<PY
import json
print(json.dumps({
    "MYSQL_ROOT_PASSWORD": "${MYSQL_ROOT_PASSWORD}",
    "MYSQL_FLYWAY_PASSWORD": "${MYSQL_FLYWAY_PASSWORD}",
    "MYSQL_APP_PASSWORD": "${MYSQL_APP_PASSWORD}",
    "MYSQL_DATABASE": "household_book",
    "OPENAI_API_KEY": "${OPENAI_API_KEY}",
    "OPENAI_API_URL": "${OPENAI_API_URL}",
}))
PY
)"

if ! aws secretsmanager describe-secret --secret-id "${SECRET_NAME}" --region "${AWS_REGION}" >/dev/null 2>&1; then
  echo "ERROR: Secret '${SECRET_NAME}' がありません。先に ./infra/scripts/deploy.sh を実行してください。" >&2
  exit 1
fi

echo "[init-secrets] Updating secret: ${SECRET_NAME}"
aws secretsmanager put-secret-value \
  --secret-id "${SECRET_NAME}" \
  --secret-string "${SECRET_JSON}" \
  --region "${AWS_REGION}" >/dev/null

echo "[init-secrets] Done. Secret name: ${SECRET_NAME}"
echo "[init-secrets] DB passwords were generated randomly and stored in Secrets Manager."
echo "[init-secrets] CORS は SSM /${PROJECT_NAME}/domain/cors-allowed-origins（CDK deploy で設定）を参照します。"
