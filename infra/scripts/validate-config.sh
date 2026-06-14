#!/usr/bin/env bash
# cdk.json + cdk.local.json の必須 context を検証する
set -euo pipefail

INFRA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/cdk-context.sh
source "${INFRA_DIR}/scripts/lib/cdk-context.sh"
cdk_context_init "${INFRA_DIR}"

LOCAL_JSON="${INFRA_DIR}/cdk.local.json"

require_context() {
  local key="$1"
  local label="$2"
  local value
  value="$(cdk_context_get "${key}")"
  if [[ -z "${value}" ]]; then
    echo "ERROR: context '${key}' が未設定です（${label}）" >&2
    echo "       ${LOCAL_JSON} に設定してください（雛形: cdk.context.example.json）" >&2
    exit 1
  fi
}

echo "[validate-config] Checking cdk.json + cdk.local.json ..."

if [[ ! -f "${LOCAL_JSON}" ]]; then
  echo "ERROR: ${LOCAL_JSON} がありません。" >&2
  echo "       cp infra/cdk.context.example.json infra/cdk.local.json して編集してください。" >&2
  exit 1
fi

require_context "domainName" "例: app.example.com"
require_context "hostedZoneName" "例: example.com"
require_context "hostedZoneId" "Route 53 ホストゾーン ID (Z...)"
require_context "certbotEmail" "Let's Encrypt 通知用メール"
require_context "cognitoUserPoolId" "既存 Cognito User Pool ID"
require_context "cognitoClientId" "既存 Cognito App Client ID"

DOMAIN="$(cdk_context_get domainName)"
ZONE="$(cdk_context_get hostedZoneName)"
if [[ "${DOMAIN}" != "${ZONE}" && "${DOMAIN}" != *".${ZONE}" ]]; then
  echo "ERROR: domainName '${DOMAIN}' は hostedZoneName '${ZONE}' のサブドメインである必要があります" >&2
  exit 1
fi

echo "[validate-config] OK"
echo "[validate-config] デプロイ前に Cognito App Client に以下を登録してください:"
echo "  Callback URL / Sign-out URL: https://${DOMAIN}/"
