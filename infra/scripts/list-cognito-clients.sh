#!/usr/bin/env bash
# Cognito User Pool の App Client ID を一覧表示（cdk.local.json の cognitoClientId 設定用）
set -euo pipefail

INFRA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"

DEFAULT_POOL_ID=""
if [[ -f "${INFRA_DIR}/cdk.local.json" ]]; then
  DEFAULT_POOL_ID="$(python3 "${INFRA_DIR}/scripts/lib/read-context.py" "${INFRA_DIR}" cognitoUserPoolId)"
fi

POOL_ID="${1:-${DEFAULT_POOL_ID}}"

if ! command -v aws >/dev/null 2>&1; then
  echo "ERROR: aws CLI が必要です" >&2
  exit 1
fi

if [[ -z "${POOL_ID}" ]]; then
  echo "ERROR: User Pool ID が未指定です。" >&2
  echo "  用法: $0 <user-pool-id>" >&2
  echo "  または infra/cdk.local.json に cognitoUserPoolId を設定してください。" >&2
  exit 1
fi

echo "[list-cognito-clients] User Pool: ${POOL_ID}"
aws cognito-idp list-user-pool-clients \
  --user-pool-id "${POOL_ID}" \
  --region "${AWS_REGION}" \
  --query 'UserPoolClients[].[ClientName,ClientId]' \
  --output table
