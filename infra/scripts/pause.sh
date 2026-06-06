#!/usr/bin/env bash
# EC2 を停止してコンピュート課金を抑える（EBS データは残る）
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

echo "[pause] Stopping instance: ${INSTANCE_ID}"
aws ec2 stop-instances --instance-ids "${INSTANCE_ID}" --region "${AWS_REGION}" >/dev/null
aws ec2 wait instance-stopped --instance-ids "${INSTANCE_ID}" --region "${AWS_REGION}"
echo "[pause] 停止しました。再開: aws ec2 start-instances --instance-ids ${INSTANCE_ID} --region ${AWS_REGION}"
