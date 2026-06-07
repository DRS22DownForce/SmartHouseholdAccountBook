# AWS インフラ学習ドキュメント

> このフォルダは、Smart Household Account Book を **AWS 上で動かすためのインフラ**（CDK、EC2、Route 53、Secrets Manager など）を学習用にまとめたものです。

## 学習ロードマップ

番号順に読むと、全体像からデプロイ手順、EC2 上の実行環境、運用までの流れがつながります。

| 章 | タイトル | ひとことで |
|----|----------|-----------|
| [01](./01-overview.md) | 全体像とアーキテクチャ | 何をどこに載せているか |
| [02](./02-cdk-stack.md) | CDK スタック | Java でインフラをコード化する |
| [03](./03-deployment-flow.md) | デプロイの流れ | deploy → secrets → deploy-app |
| [04](./04-ec2-bootstrap.md) | EC2 ブートストラップ | 初回起動とアプリ更新 |
| [05](./05-networking-security.md) | ネットワークとセキュリティ | VPC、SG、HTTPS、IAM |
| [06](./06-operations.md) | 運用と課金 | 停止・削除・トラブルシュート |

## 前提となる資料

AWS デプロイは、ローカル開発で使う Docker / MySQL の知識とつながっています。

| 内容 | 参照先 |
|------|--------|
| Docker Compose、single-host 構成 | [02. Docker](../02-docker.md) |
| MySQL ユーザー、Flyway、接続 URL | [03. MySQL](../03-mysql.md) |
| バックエンドの Spring Security / Cognito | [docs/backend/](../backend/README.md) |

実装のクイックリファレンスは [`infra/README.md`](../../../infra/README.md) を参照してください。
