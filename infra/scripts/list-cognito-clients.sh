#!/usr/bin/env bash
# Cognito User Pool の App Client ID を一覧表示（cdk.local.json の cognitoClientId 設定用）
set -euo pipefail

POOL_ID="${1:-ap-northeast-1_XXXXXXXXX}"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"

if ! command -v aws >/dev/null 2>&1; then
  echo "ERROR: aws CLI が必要です" >&2
  exit 1
fi

echo "[list-cognito-clients] User Pool: ${POOL_ID}"
aws cognito-idp list-user-pool-clients \
  --user-pool-id "${POOL_ID}" \
  --region "${AWS_REGION}" \
  --query 'UserPoolClients[].[ClientName,ClientId]' \
  --output table
