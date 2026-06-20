# AWS CDK (Java) — Smart Household Account Book

案 A: **EC2 + Docker Compose + 既存 Cognito + Route 53 + HTTPS (Let's Encrypt)**

## 事前準備（必須）

1. **`infra/cdk.json`** … 共有してよい設定（リージョン、インスタンスタイプ、Git URL など）
2. **`infra/cdk.local.json`** … 環境固有・個人情報（**Git にコミットしない**）

初回は雛形をコピーして編集します:

```bash
cp infra/cdk.context.example.json infra/cdk.local.json
# cdk.local.json を編集
```

`cdk.local.json` は **Java 側（InfraApp）が自動読み込み** します。CDK CLI のバージョンに依存しません。

| ファイル | キー | 説明 | 取得方法 |
|----------|------|------|----------|
| `cdk.local.json` | `domainName` | アプリ URL | 例: `app.example.com` |
| `cdk.local.json` | `hostedZoneName` | Route 53 ゾーン名 | 例: `example.com` |
| `cdk.local.json` | `hostedZoneId` | ホストゾーン ID | Route 53 コンソール (`Z...`) |
| `cdk.local.json` | `certbotEmail` | Let's Encrypt 通知先 | あなたのメール |
| `cdk.local.json` | `cognitoUserPoolId` | 既存 User Pool ID | Cognito コンソール |
| `cdk.local.json` | `cognitoClientId` | 既存 App Client ID | Cognito コンソール |
| `cdk.json` | `gitRepositoryUrl` | EC2 が clone する URL | （推奨）公開 Git |

**Cognito コンソール**で App Client に以下を追加:

- 許可コールバック URL: `https://smart-household-account-book.com/`
- 許可サインアウト URL: `https://smart-household-account-book.com/`
- （`www.` からもアクセスする場合は `https://www.smart-household-account-book.com/` も追加）

設定確認:

```bash
./infra/scripts/validate-config.sh
```

## デプロイ手順

```bash
./infra/scripts/deploy.sh          # 1. インフラ（初回・User Data 修正後は再実行）
./infra/scripts/init-secrets.sh    # 2. Secrets（DB パスワード等）
./infra/scripts/deploy-app.sh      # 3. ECR push + EC2 更新（Backend + Frontend・SSM のみ）
```

`deploy.sh` は CDK デプロイ前に `docker/compose`・`docker/mysql` を bootstrap 同梱用ディレクトリへ自動同期します（生成物のため Git 管理外。正本は `docker/`）。

`deploy-app.sh` は EC2 が未セットアップでも SSM 経由で bootstrap を実行します。  
**t4g.small (2GB RAM)** を前提に Next.js も EC2 上でビルドします（micro では OOM になります）。  
**t4g (ARM64)** 向けに Backend イメージを `--platform linux/arm64` でビルドします（ローカル PC が Intel でも可）。

## インスタンスサイズ

| タイプ | RAM | 用途 |
|--------|-----|------|
| `t4g.micro` | 1 GB | Backend のみ（Frontend ビルドは OOM） |
| **`t4g.small`**（既定） | 2 GB | Backend + Frontend をスクリプトデプロイ可能 |

サイズ変更後は `./infra/scripts/deploy.sh` → `./infra/scripts/deploy-app.sh` の順で実行してください。

初回の `deploy-app.sh` 実行時に **certbot** が Let's Encrypt 証明書を取得し、HTTP → HTTPS リダイレクトを設定します。

## 課金を止める

| 目的 | コマンド |
|------|----------|
| 一時停止 | `./infra/scripts/pause.sh` |
| 再開 | `./infra/scripts/resume.sh` |
| 完全削除 | `./infra/scripts/destroy.sh` |

## HTTPS について

- EC2 上の Nginx + **Let's Encrypt (certbot)** を使用（ACM は ALB 等専用のため EC2 では未使用）
- Route 53 **A レコード（apex）** → **Elastic IP** → EC2  
  デプロイ時に既存 A（133.242.164.17）が **Elastic IP に置き換わります**
- `www` は既存 CNAME → apex のまま EC2 向けになります
- EBS 暗号化で DB 保存時暗号化

## AWS 認証

```bash
aws configure
aws sts get-caller-identity   # 成功すること
```
