#!/usr/bin/env bash
# 使い方: ./docker/scripts/stack.sh <up|down> <dev|single-host-local|single-host-prod> [-v|--volumes]
#   up   … docker compose up（single-host 系は --build）。dev / single-host-* は続けて npm run dev
#   down … ポート 3000 の Next.js（npm run dev）を止めたうえで compose down
#          第 3 引数に -v または --volumes を付けると、Compose 管理の名前付きボリュームも削除（DB データ消去）
set -e #エラーが発生したらスクリプトを終了させる
cd "$(dirname "$0")/../.." #ルートディレクトリに移動
ROOT="$(pwd)"

dc() {
  docker compose --project-directory "$ROOT" --env-file "$ROOT/.env" "$@" #@はdcコマンドの引数を渡す。
}

#Next.jsの開発サーバーを停止する関数
stop_next_dev() {
  if command -v fuser >/dev/null 2>&1; then
    if fuser -k 3000/tcp >/dev/null 2>&1; then
      echo "[stack] Next.js 開発サーバー（TCP 3000 を使用中のプロセス）を停止しました。"
    else
      echo "[stack] TCP 3000 を使用中のプロセスはありませんでした（すでに停止している可能性があります）。"
    fi
  else
    echo "[stack] fuser コマンドが見つかりません。"
  fi
}

action="${1:-}" #第1引数をactionに代入
mode="${2:-}" #第2引数をmodeに代入
extra="${3:-}" #第3引数（down 時のみ: -v / --volumes でボリューム削除）

# compose down に渡す追加オプション（空ならボリュームは残す）
down_extra=()

if [[ "$action" != "up" && "$action" != "down" ]]; then
  echo "使い方: $0 <up|down> <dev|single-host-local|single-host-prod> [-v|--volumes]" >&2
  exit 1
fi

if [[ "$mode" != "dev" && "$mode" != "single-host-local" && "$mode" != "single-host-prod" ]]; then
  echo "使い方: $0 <up|down> <dev|single-host-local|single-host-prod> [-v|--volumes]" >&2
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
    echo "使い方: $0 <up|down> <dev|single-host-local|single-host-prod> [-v|--volumes]" >&2
    exit 1
  fi
fi

if [[ "$action" == "down" ]]; then
  stop_next_dev
fi

case "$mode" in
  dev)
    if [[ "$action" == "up" ]]; then
      dc -f docker/compose/docker-compose.dev.yaml up -d
      cd frontend-nextjs && npm run dev
    else
      dc -f docker/compose/docker-compose.dev.yaml down "${down_extra[@]}"
    fi
    ;;
  single-host-local)
    if [[ "$action" == "up" ]]; then
      dc -f docker/compose/docker-compose.single-host.yaml \
        -f docker/compose/docker-compose.single-host.local.yaml \
        up -d --build
      cd frontend-nextjs && npm run dev
    else
      dc -f docker/compose/docker-compose.single-host.yaml \
        -f docker/compose/docker-compose.single-host.local.yaml \
        down "${down_extra[@]}"
    fi
    ;;
  single-host-prod)
    if [[ "$action" == "up" ]]; then
      dc -f docker/compose/docker-compose.single-host.yaml \
        -f docker/compose/docker-compose.single-host.prod.yaml \
        up -d --build
      cd frontend-nextjs && npm run dev
    else
      dc -f docker/compose/docker-compose.single-host.yaml \
        -f docker/compose/docker-compose.single-host.prod.yaml \
        down "${down_extra[@]}"
    fi
    ;;
esac
