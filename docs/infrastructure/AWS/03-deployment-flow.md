# 03. デプロイの流れ：3 つのスクリプトで AWS に載せる

> この章で学ぶこと: **`deploy.sh` → `init-secrets.sh` → `deploy-app.sh` の順序**、**各スクリプトが何をするか**、**初回と 2 回目以降の違い**。

## 目次

1. [デプロイ全体の流れ](#デプロイ全体の流れ)
2. [事前準備](#事前準備)
3. [Step 1: deploy.sh（インフラ作成）](#step-1-deployshインフラ作成)
4. [Step 2: init-secrets.sh（秘密情報の投入）](#step-2-init-secretssh秘密情報の投入)
5. [Step 3: deploy-app.sh（アプリ更新）](#step-3-deploy-appshアプリ更新)
6. [初回起動と 2 回目以降](#初回起動と-2-回目以降)
7. [設定変更時の再デプロイ](#設定変更時の再デプロイ)
8. [よく使うコマンド一覧](#よく使うコマンド一覧)
9. [まず覚えるポイント](#まず覚えるポイント)

---

## デプロイ全体の流れ

AWS へのデプロイは、**3 段階**に分かれています。

```mermaid
flowchart TD
    Prep["事前準備<br/>aws configure / cdk.json"]
    D1["1. deploy.sh<br/>CDK でインフラ作成"]
    D2["2. init-secrets.sh<br/>Secrets Manager に値を入れる"]
    D3["3. deploy-app.sh<br/>ECR push + EC2 更新"]
    Done["https://your-domain でアクセス"]

    Prep --> D1 --> D2 --> D3 --> Done
```

| 段階 | 対象 | 何が起きるか |
|------|------|--------------|
| 1 | AWS インフラ | VPC、EC2、ECR、Secrets（空）、Route 53 などができる |
| 2 | 秘密情報 | DB パスワード、OpenAI キーなどが Secrets に入る |
| 3 | アプリ | Backend イメージを ECR に push し、EC2 で pull・起動する |

**順番を入れ替えないでください。** 特に Step 2 を飛ばすと、EC2 の bootstrap が DB パスワード待ちで失敗します。

---

## 事前準備

### AWS CLI の認証

```bash
aws configure
aws sts get-caller-identity   # Account と User/Role が表示されれば OK
```

### cdk.json の context

[CDK スタック（02章）](./02-cdk-stack.md#cdkjson設定の置き場所) の必須キーを `cdk.context.json` に埋めます。未設定の場合は `deploy.sh` 実行時に Java 側でエラーになります。

### Cognito App Client の URL 登録

Cognito コンソールで、App Client に次を追加します。

- 許可コールバック URL: `https://{domainName}/`
- 許可サインアウト URL: `https://{domainName}/`

### Docker（deploy-app 用）

ローカル PC で Backend イメージをビルドして ECR に push するため、Docker が必要です。EC2 は ARM64（`t4g`）のため、`deploy-app.sh` は `--platform linux/arm64` でビルドします。

---

## Step 1: deploy.sh（インフラ作成）

```bash
./infra/scripts/deploy.sh
```

### 内部で行うこと

```mermaid
flowchart LR
    B["cdk bootstrap"]
    D["cdk deploy<br/>SmartHouseholdStack"]
    EC2["EC2 起動<br/>User Data（OS 準備）"]

    B --> D --> EC2
```

| 処理 | 説明 |
|------|------|
| `npm install` | CDK CLI をローカルに入れる（初回） |
| `cdk bootstrap` | CDK 用の S3 バケットなどをアカウントに用意（初回のみ） |
| docker 設定の同期 | `docker/compose`・`docker/mysql` を bootstrap 同梱用にコピー（`deploy.sh` 内で自動実行） |
| `cdk deploy` | CloudFormation スタックを作成・更新 |

デプロイが完了すると EC2 が起動し、User Data で `aws-cli` / `unzip` がインストールされます。**アプリの bootstrap はこの時点では走りません。** `init-secrets.sh` の後に `deploy-app.sh` を実行してください。

### deploy.sh 完了後のメッセージ

スクリプトは次のステップを案内します。

```text
1. ./infra/scripts/init-secrets.sh
2. infra/cdk.json の gitRepositoryUrl を確認
3. ./infra/scripts/deploy-app.sh
```

---

## Step 2: init-secrets.sh（秘密情報の投入）

```bash
./infra/scripts/init-secrets.sh
```

### 何をするか

1. OpenAI API Key を対話的に入力（Enter でプレースホルダ可）
2. MySQL の 3 種類のパスワードを **ランダム生成**
3. Secrets Manager の `smart-household/app` に JSON を書き込む

CORS の許可 Origin は **Secrets ではなく SSM** に保存します。`deploy.sh`（CDK）が `domainName` から `/smart-household/domain/cors-allowed-origins` を自動設定します。

生成される JSON のキーは次の通りです。

| キー | 内容 |
|------|------|
| `MYSQL_ROOT_PASSWORD` | root パスワード |
| `MYSQL_FLYWAY_PASSWORD` | Flyway 用ユーザー |
| `MYSQL_APP_PASSWORD` | アプリ用ユーザー |
| `MYSQL_DATABASE` | `household_book` |
| `OPENAI_API_KEY` | OpenAI API キー |
| `OPENAI_API_URL` | Chat Completions の URL |

**パスワードは端末に表示されません。** Secrets Manager にだけ保存されます。紛失した場合はスクリプトを再実行して上書きするか、AWS コンソールで手動更新します。

### なぜ CDK の後に実行するのか

CDK が先に **Secret リソースの箱**を作り、`init-secrets.sh` が **中身**を入れる役割分担です。箱がないと `put-secret-value` は失敗します。

---

## Step 3: deploy-app.sh（アプリ更新）

```bash
./infra/scripts/deploy-app.sh
```

### 内部で行うこと

```mermaid
sequenceDiagram
    participant PC as 開発者 PC
    participant ECR
    participant SSM
    participant EC2

    PC->>PC: docker build (arm64)
    PC->>ECR: docker push :latest
    PC->>SSM: send-command
    SSM->>EC2: remote-app-deploy.sh
    EC2->>ECR: docker pull
    EC2->>EC2: compose up -d
```

| 処理 | 説明 |
|------|------|
| CloudFormation Outputs 取得 | ECR URI、InstanceId、Secret ARN など |
| `docker build --platform linux/arm64` | Backend イメージを ARM 向けにビルド |
| `docker push` | ECR の `:latest` にアップロード |
| `aws ssm send-command` | EC2 上で `remote-app-deploy.sh` を実行 |
| ポーリング | 最大 90 分、SSM の完了を待つ |

SSH ポートを開かずに EC2 を更新できるのが SSM の利点です。EC2 の IAM ロールに `AmazonSSMManagedInstanceCore` が付いています。

### remote-app-deploy.sh の分岐

EC2 上では状況に応じて処理が変わります（詳細は [EC2 ブートストラップ（04章）](./04-ec2-bootstrap.md)）。

| 状態 | 動作 |
|------|------|
| 初回（bootstrap 未完了） | `bootstrap.sh` をフル実行 |
| 通常更新 | `BOOTSTRAP_MODE=update-backend`（`.env` 再生成 → ECR pull → `compose up`） |
| Frontend 未構築 | `BOOTSTRAP_MODE=frontend-only` で Next.js ビルド |

---

## 初回起動と 2 回目以降

### 初回（インフラを作った直後）

```text
deploy.sh
  → EC2 起動、User Data で aws-cli / unzip を準備
init-secrets.sh
  → Secrets にパスワード投入
deploy-app.sh
  → ECR にイメージ push、SSM 経由で bootstrap 開始
  → Next.js ビルド（数分〜十数分かかることがある）
```

初回の Frontend ビルドは `t4g.small` でも時間がかかります。swap を追加するなど、bootstrap 側でメモリ不足対策をしています。

### 2 回目以降（アプリのコード変更のみ）

```bash
./infra/scripts/deploy-app.sh
```

インフラ（EC2 サイズ、ドメイン、User Data）を変えていなければ、**deploy-app だけ**で十分です。Backend の Docker イメージが更新され、EC2 上のコンテナが再起動します。

---

## 設定変更時の再デプロイ

| 変更内容 | 必要な操作 |
|----------|------------|
| Java / Spring のコード | `deploy-app.sh` のみ |
| `docker/compose/*.yaml` | `deploy.sh`（bootstrap 同梱用に自動同期）→ 必要に応じて `deploy-app.sh` |
| `cdk.json` の `instanceType` など | `deploy.sh`（EC2 が置き換わる場合あり） |
| Secrets の値（API キー等） | `init-secrets.sh` → `deploy-app.sh`（`.env` 再生成） |
| `domainName` / Route 53 | `cdk.json` 更新 → `deploy.sh`、Cognito URL も更新 → `deploy-app.sh`（CORS は SSM 経由で `.env` に反映） |

`deploy.sh` は CDK デプロイ前に、リポジトリの `docker/` 配下を `infra/assets/ec2-bootstrap/bundled/docker/` に自動コピーします（**Git 管理外の生成物**。正本は `docker/compose`・`docker/mysql`）。Compose や MySQL 設定を変えたら `deploy.sh` を再実行してください。`npx cdk deploy` を直接実行しないでください。

---

## よく使うコマンド一覧

### デプロイ

| コマンド | 目的 |
|----------|------|
| `./infra/scripts/deploy.sh` | インフラの作成・更新（docker 設定の bootstrap 同梱同期を含む） |
| `./infra/scripts/init-secrets.sh` | Secrets の投入・更新 |
| `./infra/scripts/deploy-app.sh` | Backend デプロイ + EC2 更新 |

### 課金・運用

| コマンド | 目的 |
|----------|------|
| `./infra/scripts/pause.sh` | EC2 を一時停止（コンピュート課金を抑える） |
| `./infra/scripts/resume.sh` | 停止した EC2 を再開 |
| `./infra/scripts/destroy.sh` | CDK スタックを完全削除 |

`hostedZoneId` や `cognitoClientId` などの初回設定値は、AWS コンソール（Route 53 / Cognito）から確認して `cdk.context.json` に記入します。

---

## まず覚えるポイント

- デプロイは **`deploy.sh` → `init-secrets.sh` → `deploy-app.sh`** の 3 段階です。
- `deploy.sh` は CDK でインフラを作り、EC2 を起動します（User Data は OS 準備のみ）。
- `init-secrets.sh` は DB パスワードなどを Secrets Manager に入れます。
- `deploy-app.sh` は ECR に push し、SSM 経由で EC2 を更新します。
- コード変更だけなら、通常は **`deploy-app.sh` だけ**で足ります。
