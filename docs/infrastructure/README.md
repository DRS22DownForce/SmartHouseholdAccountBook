# インフラ・ツールチェーン学習ドキュメント

> このフォルダは、Smart Household Account Book の**ビルド・実行環境・データベース・CI**など、アプリケーション本体の Java コード以外の周辺技術を学習用にまとめたものです。

## 学習ロードマップ

番号順に読むと、ローカル開発から CI までの流れがつながります。

| 章 | タイトル | ひとことで |
|----|----------|-----------|
| [01](./01-maven.md) | Maven | 依存関係とビルド、OpenAPI 生成 |
| [02](./02-docker.md) | Docker | コンテナと Compose で環境をそろえる |
| [03](./03-mysql.md) | MySQL | データベースの役割とこのプロジェクトでの使い方 |
| [04](./04-ci-and-pr-review.md) | CI と PR レビュー | GitHub Actions と Cursor Bugbot |

バックエンドの Spring 自体の解説は [docs/backend/README.md](../backend/README.md)、フロントエンドは [docs/frontend/README.md](../frontend/README.md) を参照してください。
