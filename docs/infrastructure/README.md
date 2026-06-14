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

### AWS（本番デプロイ）

ローカル Docker の延長として AWS に載せる流れは [AWS/](./AWS/README.md) を参照してください。

| 章 | タイトル | ひとことで |
|----|----------|-----------|
| [AWS/01](./AWS/01-overview.md) | 全体像とアーキテクチャ | EC2 1 台構成の地図 |
| [AWS/02](./AWS/02-cdk-stack.md) | CDK スタック | Java でインフラを定義 |
| [AWS/03](./AWS/03-deployment-flow.md) | デプロイの流れ | deploy → secrets → deploy-app |
| [AWS/04](./AWS/04-ec2-bootstrap.md) | EC2 ブートストラップ | 初回起動と更新 |
| [AWS/05](./AWS/05-networking-security.md) | ネットワークとセキュリティ | VPC、HTTPS、IAM |
| [AWS/06](./AWS/06-operations.md) | 運用と課金 | 停止・削除・トラブルシュート |

バックエンドの Spring 自体の解説は [docs/backend/README.md](../backend/README.md)、フロントエンドは [docs/frontend/README.md](../frontend/README.md) を参照してください。
