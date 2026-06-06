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

PUBLIC_IP="$(aws cloudformation describe-stacks \
  --stack-name SmartHouseholdStack \
  --query "Stacks[0].Outputs[?OutputKey=='PublicIp'].OutputValue" \
  --output text --region "${AWS_REGION}")"

echo "[resume] 起動しました: http://${PUBLIC_IP}"
