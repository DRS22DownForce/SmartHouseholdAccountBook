#!/usr/bin/env bash
# バックエンド Docker イメージを ECR に push し、EC2 上で bootstrap 修復 + Compose 更新（SSM）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
INFRA_DIR="${ROOT}/infra"
AWS_REGION="${AWS_REGION:-ap-northeast-1}"
PROJECT_NAME="${PROJECT_NAME:-smart-household}"

cd "${INFRA_DIR}"

if [[ ! -d node_modules ]]; then
  npm install
fi

stack_output() {
  local key="$1"
  aws cloudformation describe-stacks \
    --stack-name SmartHouseholdStack \
    --query "Stacks[0].Outputs[?OutputKey=='${key}'].OutputValue" \
    --output text \
    --region "${AWS_REGION}" 2>/dev/null || true
}

echo "[deploy-app] Resolving stack outputs..."
ECR_URI="$(stack_output BackendRepositoryUri)"
INSTANCE_ID="$(stack_output InstanceId)"
APP_SECRET_ARN="$(stack_output AppSecretArn)"
BOOTSTRAP_ASSET_URL="$(stack_output BootstrapAssetS3Url)"

if [[ -z "${ECR_URI}" || "${ECR_URI}" == "None" ]]; then
  echo "ERROR: ECR URI を取得できません。先に ./infra/scripts/deploy.sh を実行してください。" >&2
  exit 1
fi

# EC2 が t4g (ARM64) のため、ローカルが x86 でも ARM 向けにビルドする
DOCKER_PLATFORM="${DOCKER_PLATFORM:-linux/arm64}"

echo "[deploy-app] Building backend image for ${DOCKER_PLATFORM}..."
docker build --platform "${DOCKER_PLATFORM}" \
  -f "${ROOT}/backend/Dockerfile" \
  -t "${PROJECT_NAME}-backend:latest" \
  -t "${ECR_URI}:latest" \
  "${ROOT}"

echo "[deploy-app] Logging in to ECR..."
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_URI%%/*}"

echo "[deploy-app] Pushing ${ECR_URI}:latest ..."
docker push "${ECR_URI}:latest"

if [[ -z "${INSTANCE_ID}" || "${INSTANCE_ID}" == "None" ]]; then
  echo "[deploy-app] Image pushed. EC2 instance ID が不明なため SSM 更新はスキップします。"
  exit 0
fi

if [[ -z "${APP_SECRET_ARN}" || "${APP_SECRET_ARN}" == "None" ]]; then
  echo "WARN: AppSecretArn が未取得です。先に cdk deploy を再実行してください。"
  APP_SECRET_ARN="$(aws secretsmanager describe-secret --secret-id "${PROJECT_NAME}/app" --query ARN --output text --region "${AWS_REGION}" 2>/dev/null || true)"
fi

if [[ -z "${BOOTSTRAP_ASSET_URL}" || "${BOOTSTRAP_ASSET_URL}" == "None" ]]; then
  echo "ERROR: BootstrapAssetS3Url を取得できません。cdk deploy を再実行してください。" >&2
  exit 1
fi

if [[ -z "${APP_SECRET_ARN}" || "${APP_SECRET_ARN}" == "None" ]]; then
  echo "ERROR: App Secret ARN を取得できません。" >&2
  exit 1
fi

echo "[deploy-app] Updating EC2 via SSM (${INSTANCE_ID})..."
echo "[deploy-app] 未セットアップの EC2 は bootstrap 修復後に Backend/Frontend を起動します（最大 90 分）。"

SSM_PARAMS="$(PROJECT_NAME="${PROJECT_NAME}" AWS_REGION="${AWS_REGION}" ECR_URI="${ECR_URI}" \
  APP_SECRET_ARN="${APP_SECRET_ARN}" BOOTSTRAP_ASSET_URL="${BOOTSTRAP_ASSET_URL}" \
  python3 <<'PY'
import json
import os

region = os.environ["AWS_REGION"]
project = os.environ["PROJECT_NAME"]
ecr = os.environ["ECR_URI"]
secret_arn = os.environ["APP_SECRET_ARN"]
bootstrap_url = os.environ["BOOTSTRAP_ASSET_URL"]

commands = [
    "set -euxo pipefail",
    f"export PROJECT_NAME={project}",
    f"export AWS_REGION={region}",
    f"export ECR_REPO_URI={ecr}",
    f"export APP_SECRET_ARN={secret_arn}",
    f"export BOOTSTRAP_ASSET_URL={bootstrap_url}",
    "dnf install -y aws-cli unzip || true",
    "mkdir -p /opt/smart-household/bootstrap",
    f"aws s3 cp {bootstrap_url} /tmp/bootstrap.zip --region {region}",
    "unzip -o /tmp/bootstrap.zip -d /opt/smart-household/bootstrap",
    "chmod +x /opt/smart-household/bootstrap/*.sh",
    "/opt/smart-household/bootstrap/remote-app-deploy.sh",
]
print(json.dumps({"commands": commands}))
PY
)"

COMMAND_ID="$(aws ssm send-command \
  --instance-ids "${INSTANCE_ID}" \
  --document-name "AWS-RunShellScript" \
  --comment "Deploy Smart Household (bootstrap repair + backend)" \
  --timeout-seconds 7200 \
  --parameters "${SSM_PARAMS}" \
  --region "${AWS_REGION}" \
  --query Command.CommandId --output text)"

echo "[deploy-app] SSM CommandId: ${COMMAND_ID} (waiting up to 90 min)..."

poll_ssm() {
  local status=""
  for _ in $(seq 1 180); do
    status="$(aws ssm get-command-invocation \
      --command-id "${COMMAND_ID}" \
      --instance-id "${INSTANCE_ID}" \
      --region "${AWS_REGION}" \
      --query Status --output text 2>/dev/null || echo Pending)"
    case "${status}" in
      Success)
        echo "[deploy-app] SSM command succeeded."
        return 0
        ;;
      Failed|Cancelled|TimedOut)
        echo "[deploy-app] SSM command failed (status=${status}). ログ:"
        aws ssm get-command-invocation \
          --command-id "${COMMAND_ID}" \
          --instance-id "${INSTANCE_ID}" \
          --region "${AWS_REGION}"
        return 1
        ;;
      *) sleep 30 ;;
    esac
  done
  echo "[deploy-app] SSM command timed out waiting for completion."
  aws ssm get-command-invocation \
    --command-id "${COMMAND_ID}" \
    --instance-id "${INSTANCE_ID}" \
    --region "${AWS_REGION}" || true
  return 1
}

poll_ssm

APP_URL="$(stack_output AppUrl)"
if [[ -z "${APP_URL}" || "${APP_URL}" == "None" ]]; then
  APP_URL="$(stack_output ElasticIp)"
fi

echo "[deploy-app] Done."
echo "[deploy-app] App URL: ${APP_URL}"
