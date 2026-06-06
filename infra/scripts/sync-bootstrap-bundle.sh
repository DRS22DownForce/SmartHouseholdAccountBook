#!/usr/bin/env bash
# docker/ 配下の compose / mysql 設定を bootstrap 同梱用にコピーする
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BUNDLE="${ROOT}/infra/assets/ec2-bootstrap/bundled/docker"

rm -rf "${BUNDLE}/compose" "${BUNDLE}/mysql"
mkdir -p "${BUNDLE}"
cp -a "${ROOT}/docker/compose" "${BUNDLE}/"
cp -a "${ROOT}/docker/mysql" "${BUNDLE}/"
echo "[sync-bootstrap-bundle] Updated ${BUNDLE}"
