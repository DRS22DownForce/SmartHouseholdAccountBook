#!/usr/bin/env bash
# cdk.json + cdk.local.json の共通ヘルパー（他スクリプトから source する）
set -euo pipefail

cdk_context_init() {
  local infra_dir="$1"
  INFRA_DIR="${infra_dir}"
  READ_CONTEXT="${INFRA_DIR}/scripts/lib/read-context.py"
}

cdk_context_get() {
  local key="$1"
  python3 "${READ_CONTEXT}" "${INFRA_DIR}" "${key}"
}
