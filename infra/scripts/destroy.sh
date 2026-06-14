#!/usr/bin/env bash
# スタックを完全削除して課金を止める
set -euo pipefail

INFRA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"

cd "${INFRA_DIR}"

if [[ ! -d node_modules ]]; then
  npm install
fi

export CDK_DEFAULT_REGION="${AWS_REGION}"
if command -v aws >/dev/null 2>&1; then
  export CDK_DEFAULT_ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
fi

echo "[destroy] 以下のスタックを削除します: SmartHouseholdStack"
read -r -p "続行しますか? [y/N]: " CONFIRM
if [[ "${CONFIRM}" != "y" && "${CONFIRM}" != "Y" ]]; then
  echo "キャンセルしました。"
  exit 0
fi

npx cdk destroy SmartHouseholdStack --force

echo "[destroy] 完了。EC2 / EBS / Cognito / ECR / Secrets が削除されました（RemovalPolicy 設定に依存）。"
