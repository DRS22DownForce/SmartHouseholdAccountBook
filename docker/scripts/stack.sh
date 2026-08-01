#!/usr/bin/env bash
# 使い方: ./docker/scripts/stack.sh <up|down> <db|dev|prod> [-v|--volumes]
#   db   … MySQL のみ（IDE で Spring を動かす用）
#   dev  … MySQL + Backend + Frontend(dev / バインドマウント)
#   prod … MySQL + Backend + Frontend(standalone)
#   down 時に -v / --volumes を付けると名前付きボリュームも削除（DB データ消去）
#
# SPRING_PROFILES_ACTIVE は .env を参照（未設定時: up dev → dev、up prod → prod）
set -e #エラーが発生したらスクリプトを終了させる
cd "$(dirname "$0")/../.." #ルートディレクトリに移動
ROOT="$(pwd)"
COMPOSE_FILE="docker/compose/docker-compose.yaml"

dc() {
  docker compose --project-directory "$ROOT" --env-file "$ROOT/.env" -f "$COMPOSE_FILE" "$@" #@はdcコマンドの引数を渡す。
}

action="${1:-}"
mode="${2:-}"
extra="${3:-}"
down_extra=()

usage() {
  echo "使い方: $0 <up|down> <db|dev|prod> [-v|--volumes]" >&2
}

if [[ "$action" != "up" && "$action" != "down" ]]; then
  usage
  exit 1
fi

if [[ "$mode" != "db" && "$mode" != "dev" && "$mode" != "prod" ]]; then
  usage
  exit 1
fi

if [[ -n "$extra" ]]; then
  if [[ "$action" != "down" ]]; then
    echo "エラー: -v / --volumes は down のときだけ指定できます。" >&2
    exit 1
  fi
  if [[ "$extra" == "-v" || "$extra" == "--volumes" ]]; then
    down_extra=(-v)
  else
    usage
    exit 1
  fi
fi

case "$mode" in
  db)
    if [[ "$action" == "up" ]]; then
      dc --profile db up -d # dcの後に続くのがdcコマンドの引数。
    else
      dc --profile db down "${down_extra[@]}"
    fi
    ;;
  dev)
    if [[ "$action" == "up" ]]; then
      # .env 未設定時にSpringのプロファイルをdevに設定するフォールバック
      export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
      dc --profile dev up -d --build
    else
      dc --profile dev down "${down_extra[@]}"
    fi
    ;;
  prod)
    if [[ "$action" == "up" ]]; then
      # Springのプロファイルをprodに設定する。（.env が dev のままでもここは prod）
      export SPRING_PROFILES_ACTIVE=prod
      dc --profile prod up -d --build
    else
      dc --profile prod down "${down_extra[@]}"
    fi
    ;;
esac
