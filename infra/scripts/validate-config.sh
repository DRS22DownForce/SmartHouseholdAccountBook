#!/usr/bin/env bash
# cdk.json の必須 context を検証する
set -euo pipefail

INFRA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CDK_JSON="${INFRA_DIR}/cdk.json"

read_context() {
  local key="$1"
  python3 -c "import json,sys; print(json.load(open('${CDK_JSON}'))['context'].get('${key}','') or '')"
}

require_context() {
  local key="$1"
  local label="$2"
  local value
  value="$(read_context "${key}")"
  if [[ -z "${value}" ]]; then
    echo "ERROR: cdk.json context '${key}' が未設定です（${label}）" >&2
    exit 1
  fi
}

echo "[validate-config] Checking ${CDK_JSON} ..."

require_context "domainName" "例: app.example.com"
require_context "hostedZoneName" "例: example.com"
require_context "hostedZoneId" "Route 53 ホストゾーン ID (Z...)"
require_context "certbotEmail" "Let's Encrypt 通知用メール"
require_context "cognitoUserPoolId" "既存 Cognito User Pool ID"
require_context "cognitoClientId" "既存 Cognito App Client ID"

DOMAIN="$(read_context domainName)"
ZONE="$(read_context hostedZoneName)"
if [[ "${DOMAIN}" != "${ZONE}" && "${DOMAIN}" != *".${ZONE}" ]]; then
  echo "ERROR: domainName '${DOMAIN}' は hostedZoneName '${ZONE}' のサブドメインである必要があります" >&2
  exit 1
fi

echo "[validate-config] OK"
echo "[validate-config] デプロイ前に Cognito App Client に以下を登録してください:"
echo "  Callback URL / Sign-out URL: https://${DOMAIN}/"
