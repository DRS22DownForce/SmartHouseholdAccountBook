#!/usr/bin/env bash
# pause.sh で停止した EC2 を再開する
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-1}"

INSTANCE_ID="$(aws cloudformation describe-stacks \
  --stack-name SmartHouseholdStack \
  --query "Stacks[0].Outputs[?OutputKey=='InstanceId'].OutputValue" \
  --output text --region "${AWS_REGION}" 2>/dev/null || true)"

if [[ -z "${INSTANCE_ID}" || "${INSTANCE_ID}" == "None" ]]; then
  echo "ERROR: SmartHouseholdStack が見つかりません。" >&2
  exit 1
fi

echo "[resume] Starting instance: ${INSTANCE_ID}"
aws ec2 start-instances --instance-ids "${INSTANCE_ID}" --region "${AWS_REGION}" >/dev/null
aws ec2 wait instance-running --instance-ids "${INSTANCE_ID}" --region "${AWS_REGION}"

APP_URL="$(aws cloudformation describe-stacks \
  --stack-name SmartHouseholdStack \
  --query "Stacks[0].Outputs[?OutputKey=='AppUrl'].OutputValue" \
  --output text --region "${AWS_REGION}" 2>/dev/null || true)"

if [[ -z "${APP_URL}" || "${APP_URL}" == "None" ]]; then
  APP_URL="https://$(aws cloudformation describe-stacks \
    --stack-name SmartHouseholdStack \
    --query "Stacks[0].Outputs[?OutputKey=='ElasticIp'].OutputValue" \
    --output text --region "${AWS_REGION}")"
fi

echo "[resume] 起動しました: ${APP_URL}"
