#!/usr/bin/env bash
# Route 53 ホストゾーン ID を一覧表示（cdk.json の hostedZoneId 設定用）
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-1}"
ZONE_NAME="${1:-}"

if ! command -v aws >/dev/null 2>&1; then
  echo "ERROR: aws CLI が必要です" >&2
  exit 1
fi

if [[ -n "${ZONE_NAME}" ]]; then
  aws route53 list-hosted-zones-by-name --dns-name "${ZONE_NAME}" --query 'HostedZones[0].[Name,Id]' --output text
else
  echo "Usage: $0 example.com"
  echo "Or list all:"
  aws route53 list-hosted-zones --query 'HostedZones[].[Name,Id]' --output table --region "${AWS_REGION}" 2>/dev/null || \
    aws route53 list-hosted-zones --query 'HostedZones[].[Name,Id]' --output table
fi
