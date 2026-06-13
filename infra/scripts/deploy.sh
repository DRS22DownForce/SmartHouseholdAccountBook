#!/usr/bin/env bash
# インフラを AWS にデプロイする（CDK）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
INFRA_DIR="${ROOT}/infra"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"

cd "${INFRA_DIR}"

if ! command -v aws >/dev/null 2>&1; then
  echo "ERROR: aws CLI が必要です" >&2
  exit 1
fi

"${INFRA_DIR}/scripts/validate-config.sh"

if [[ ! -d node_modules ]]; then # node_modules はnpmがインストールしたパッケージを管理するディレクトリ
  echo "[deploy] Installing CDK CLI (local npm)..."
  npm install
fi

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
export CDK_DEFAULT_ACCOUNT="${ACCOUNT_ID}"
export CDK_DEFAULT_REGION="${AWS_REGION}"

echo "[deploy] Bootstrapping CDK (初回のみ)..."
npx cdk bootstrap "aws://${ACCOUNT_ID}/${AWS_REGION}" #CDKがデプロイするための共通の土台をAWSに作成する

echo "[deploy] Deploying SmartHouseholdStack..."
npx cdk deploy SmartHouseholdStack --require-approval never #CDKでSmartHouseholdStackに定義したスタックをAWSにデプロイする

echo ""
echo "[deploy] 完了。次のステップ:"
echo "  1. ./infra/scripts/init-secrets.sh"
echo "  2. infra/cdk.json の gitRepositoryUrl に Git リポジトリ URL を設定（変更後は cdk deploy を再実行）"
echo "  3. ./infra/scripts/deploy-app.sh"
