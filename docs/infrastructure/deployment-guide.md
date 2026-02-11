# さくらのVPS Ubuntuサーバーへのデプロイガイド

> Smart Household Account BookプロジェクトをさくらのVPSのUbuntuサーバーにデプロイするための初心者向けガイドです。セキュリティとパフォーマンスを重視した構成を説明します。

## 📋 目次

1. [デプロイ構成の概要](#デプロイ構成の概要)
2. [前提条件](#前提条件)
3. [サーバーの初期セットアップ](#サーバーの初期セットアップ)
4. [必要なソフトウェアのインストール](#必要なソフトウェアのインストール)
5. [DNS設定](#dns設定)
6. [プロジェクトのデプロイ準備](#プロジェクトのデプロイ準備)
7. [データベース（MySQL）のセットアップ](#データベースmysqlのセットアップ)
8. [バックエンド（Spring Boot）のデプロイ](#バックエンドspring-bootのデプロイ)
9. [フロントエンド（Next.js）のデプロイ](#フロントエンドnextjsのデプロイ)
10. [Nginxリバースプロキシの設定](#nginxリバースプロキシの設定)
11. [SSL証明書の設定（Let's Encrypt）](#ssl証明書の設定lets-encrypt)
12. [コードの更新と再デプロイ](#コードの更新と再デプロイ)
13. [セキュリティ設定](#セキュリティ設定)
14. [監視とログ管理](#監視とログ管理)
15. [トラブルシューティング](#トラブルシューティング)

---

## デプロイ構成の概要

### アーキテクチャ図

```
┌─────────────────────────────────────────────────────────┐
│                    インターネット                         │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTPS (443) / HTTP (80)
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  Nginx (リバースプロキシ)                 │
│  - SSL/TLS終端、静的ファイル配信、APIプロキシ            │
└──────┬──────────────────────────────┬───────────────────┘
       │ /api/*                        │ /
       ▼                              ▼
┌──────────────────┐         ┌──────────────────┐
│  Spring Boot      │         │   Next.js        │
│  (ポート: 8080)   │         │  (ポート: 3000)  │
└────────┬─────────┘         └──────────────────┘
         │ JDBC
         ▼
┌──────────────────┐
│   MySQL 8.0      │
│  (ポート: 3306)  │
└──────────────────┘
```

### 構成の説明

**各コンポーネントの役割**:
- **Nginx**: Webサーバーとリバースプロキシ。外部リクエストを受け取り、適切なアプリケーションに振り分けます。Next.jsの静的ファイルを直接配信してパフォーマンスを最適化します。
- **Next.js**: フロントエンドアプリケーション。静的ファイルはNginxが直接配信し、動的ページ（SSR）のみNext.jsサーバーで処理されます。
- **Spring Boot**: バックエンドAPIサーバー。データベースとのやり取りやビジネスロジックを処理します。
- **MySQL**: データベース。支出データなどの情報を保存します。

**リクエストの振り分け**:
- `/_next/static/*`: 静的ファイル → Nginxがファイルシステムから直接配信（高速）
- `/api/*`: APIリクエスト → Spring Bootにプロキシ
- その他（`/`）: 動的ページ（SSR） → Next.jsサーバーにプロキシ

**Docker Composeファイルの構成**:
本番環境では、MySQLとSpring Bootを**別々のDocker Composeファイル**で管理します：
- **`docker-compose.mysql.yaml`**: MySQLのみを起動（データベースの独立性を確保）
- **`docker-compose.backend.yaml`**: Spring Bootのみを起動（既存のMySQLコンテナに接続）

**分離する理由**: 独立性、リソース管理、スケーラビリティ、セキュリティの向上

### ポート構成

| サービス | 内部ポート | 外部公開 | 説明 |
|---------|----------|---------|------|
| Nginx | 80, 443 | ✅ | HTTP/HTTPSアクセス |
| Next.js | 3000 | ❌ | 内部のみ（Nginx経由） |
| Spring Boot | 8080 | ❌ | 内部のみ（Nginx経由） |
| MySQL | 3306 | ❌ | 内部のみ（セキュリティのため） |

**内部ポートを外部公開しない理由**: セキュリティ向上のため。Nginxが唯一の入口となり、ファイアウォールで他のポートを閉じることができます。

---

## 前提条件

### 必要なもの

1. **さくらのVPSサーバー**
   - OS: Ubuntu 22.04 LTS 以上（推奨: Ubuntu 22.04 または 24.04）
   - メモリ: 最低2GB（推奨: 4GB以上）
   - ストレージ: 最低20GB（推奨: 40GB以上）
   - ルート権限（sudo）を持つユーザーアカウント

2. **ドメイン名**（オプション、推奨）
   - SSL証明書を取得するために必要
   - **既にRoute53でドメインを持っている場合**: 新規購入は不要。DNS設定のみ必要

3. **AWS Cognito設定**
   - Cognito User Poolの設定が完了していること
   - JWK Set URLが取得できていること

4. **ローカル開発環境**
   - Gitがインストールされていること
   - SSH接続が可能なこと

### 確認事項

デプロイ前に以下を確認してください：
- [ ] VPSサーバーにSSH接続できること
- [ ] ドメイン名のDNS設定が完了していること（AレコードでVPSのIPアドレスを指す）
- [ ] AWS Cognitoの設定が完了していること
- [ ] 環境変数の値が準備できていること

---

## サーバーの初期セットアップ

### ステップ1: SSH接続

```bash
# サーバーにSSH接続（IPアドレスとユーザー名を置き換えてください）
ssh username@your-server-ip
```

**初心者向けの解説**: SSH（Secure Shell）はサーバーに安全に接続するためのプロトコルです。初回接続時は、サーバーのフィンガープリントを確認するメッセージが表示されます。`yes`と入力して続行します。

### ステップ2: システムの更新

```bash
# パッケージリストの更新
sudo apt update

# インストール済みパッケージのアップグレード
sudo apt upgrade -y

# システムの再起動（必要に応じて）
sudo reboot
```

**初心者向けの解説**: `apt update`はパッケージリストを最新化し、`apt upgrade`は既存パッケージを最新版に更新します。セキュリティパッチが含まれることが多いため、重要です。

### ステップ3: ファイアウォールの設定

```bash
# UFWの有効化
sudo ufw enable

# SSH接続を許可（重要！これを先に設定しないと接続できなくなる可能性があります）
sudo ufw allow 22/tcp

# HTTP（ポート80）を許可
sudo ufw allow 80/tcp

# HTTPS（ポート443）を許可
sudo ufw allow 443/tcp

# ファイアウォールの状態を確認
sudo ufw status
```

**初心者向けの解説**: UFW（Uncomplicated Firewall）はLinuxのファイアウォールを簡単に設定できるツールです。SSH（22番ポート）を最初に許可しないと、次回接続できなくなる可能性があります。

---

## 必要なソフトウェアのインストール

### ステップ1: DockerとDocker Composeのインストール

```bash
# 必要なパッケージのインストール
sudo apt install -y ca-certificates curl gnupg lsb-release

# Dockerの公式GPGキーを追加
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Dockerのリポジトリを追加
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# パッケージリストを更新
sudo apt update

# Docker EngineとDocker Composeをインストール
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Dockerサービスを起動・有効化
sudo systemctl start docker
sudo systemctl enable docker

# 現在のユーザーをdockerグループに追加（sudoなしでDockerを使えるようにする）
sudo usermod -aG docker $USER

# インストールの確認
docker --version
docker compose version
```

**初心者向けの解説**: Dockerはアプリケーションとその依存関係を「コンテナ」という単位でパッケージ化する技術です。Docker Composeは複数のコンテナを管理するためのツールです。

**重要**: ユーザーをdockerグループに追加した後、一度ログアウトして再ログインする必要があります。

```bash
# ログアウト
exit

# 再度SSH接続
ssh username@your-server-ip
```

### ステップ2: Node.jsとnpmのインストール

```bash
# Node.js 20.x（LTS版）をインストール
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# インストールの確認
node --version
npm --version
```

**初心者向けの解説**: Node.jsはJavaScriptをサーバー側で実行するためのランタイムです。Next.jsはNode.js上で動作します。npmはNode.jsのパッケージ（ライブラリ）を管理するツールです。

### ステップ3: Javaのインストール（OpenAPI Generator用）

```bash
# OpenJDK 17をインストール
sudo apt update
sudo apt install -y openjdk-17-jdk

# インストールの確認
java -version
javac -version
```

**初心者向けの解説**: OpenAPI Generator CLIは内部的にJavaを使用してコード生成を行います。フロントエンドのAPIクライアント生成（`npm run generate:api`）に必要です。

### ステップ4: Nginxのインストール

```bash
# Nginxのインストール
sudo apt install -y nginx

# Nginxサービスを起動・有効化
sudo systemctl start nginx
sudo systemctl enable nginx

# インストールの確認
nginx -v
```

**初心者向けの解説**: Nginxは高性能なWebサーバーです。静的ファイルの配信や、リクエストを別のサーバーに転送（リバースプロキシ）する役割を担います。

### ステップ5: Certbotのインストール（SSL証明書用）

```bash
# Certbotのインストール
sudo apt install -y certbot python3-certbot-nginx
```

**初心者向けの解説**: CertbotはLet's EncryptからSSL証明書を自動的に取得・更新するツールです。SSL証明書はHTTPS通信を有効にするために必要な証明書です。

---

## DNS設定

### サーバー側のDNS設定（オプション）

さくらのVPSのデフォルト設定では、外部ドメインの名前解決ができない場合があります。この場合、netplanでDNSサーバーを設定します。

```bash
# netplanの設定ファイルを確認
cat /etc/netplan/01-netcfg.yaml

# 設定ファイルを編集（ファイル名は環境によって異なります）
sudo nano /etc/netplan/01-netcfg.yaml
```

設定ファイルの`nameservers`セクションに、パブリックDNSサーバー（例: `1.1.1.1`, `8.8.8.8`）を追加します：

```yaml
network:
  version: 2
  ethernets:
    "ens3":
      gateway4: YOUR_GATEWAY_IP  # 実際のゲートウェイIPアドレスに置き換えてください
      nameservers:
        addresses: [YOUR_NAMESERVER_IP_1, YOUR_NAMESERVER_IP_2, 1.1.1.1, 8.8.8.8]  # 実際のDNSサーバーIPアドレスに置き換えてください
      # ... その他の設定 ...
```

設定を適用します：

```bash
# 設定ファイルの構文をチェック
sudo netplan try

# 問題がなければ、設定を適用
sudo netplan apply

# DNS設定が正しく適用されたか確認
resolvectl status ens3

# 実際に名前解決ができるか確認
dig +short A your-domain.com  # 実際のドメイン名に置き換えてください
```

**初心者向けの解説**: nameserversはDNSサーバーのIPアドレスを指定します。1.1.1.1（Cloudflare）と8.8.8.8（Google）は高速で安定性が高いDNSサーバーです。

### ドメインのDNS設定（Route53の場合）

**既にAWS Route53でドメインを持っている場合**、新規購入は不要です。Route53でDNS設定を行うだけで使用できます。

#### ステップ1: VPSのIPアドレスを確認

```bash
# VPSサーバーにSSH接続して、IPアドレスを確認
curl ifconfig.me
# または
hostname -I
```

#### ステップ2: Route53でAレコードを設定

1. **AWSマネジメントコンソール**にログイン
2. **Route53**サービスを開く
3. 左メニューから「**ホストゾーン**」をクリック
4. ドメインのホストゾーンを確認（なければ作成）

**レコード1: ルートドメイン用（必須）**
- **レコード名**: （空白、または @ を入力）
- **レコードタイプ**: A
- **値**: VPSのIPアドレス
- **TTL**: 300（5分、または任意の値）
- **ルーティングポリシー**: シンプルルーティング

**レコード2: www用（オプション、推奨）**
- **レコード名**: `www`
- **レコードタイプ**: A
- **値**: VPSのIPアドレス（同じIPアドレス）
- **TTL**: 300
- **ルーティングポリシー**: シンプルルーティング

#### ステップ3: DNS設定の反映確認

```bash
# ドメインが正しく設定されているか確認
nslookup your-domain.com  # 実際のドメイン名に置き換えてください
# または
dig your-domain.com  # 実際のドメイン名に置き換えてください
```

**初心者向けの解説**: Aレコードはドメイン名をIPアドレスに変換するDNSレコードです。Route53の変更は通常数分で反映されますが、最大48時間かかる場合もあります。

**注意**: DNS設定が反映されるまで、SSL証明書の取得（Certbot）は実行できません。必ず`nslookup`で確認してから次のステップに進んでください。

---

## プロジェクトのデプロイ準備

### ステップ1: プロジェクトのクローン

```bash
# ホームディレクトリに移動
cd ~

# プロジェクトをクローン（URLを実際のリポジトリURLに置き換えてください）
git clone https://github.com/your-username/SmartHouseholdAccountBook.git

# プロジェクトディレクトリに移動
cd SmartHouseholdAccountBook
```

**初心者向けの解説**: Git cloneはリモートリポジトリ（GitHubなど）からプロジェクトのコードをダウンロードするコマンドです。プライベートリポジトリの場合は、認証情報が必要になることがあります。

### ステップ2: 環境変数ファイルの作成

**重要**: 開発環境と本番環境で環境変数を分離することを強く推奨します。

本番環境用の環境変数ファイルを作成します：

```bash
# プロジェクトルートに本番環境用の.env.productionファイルを作成
nano .env.production
```

以下の内容を記述します（実際の値に置き換えてください）：

```env
# ========================================
# MySQL設定
# ========================================
MYSQL_ROOT_USER=root
MYSQL_ROOT_PASSWORD=your-secure-password-here
MYSQL_DATABASE=smart_household_db

# ========================================
# Spring Boot データソース設定
# ========================================
# 本番環境用のMySQL接続URL
SPRING_DATASOURCE_URL_PROD=jdbc:mysql://mysql:3306/smart_household_db?useSSL=true&serverTimezone=Asia/Tokyo

# ========================================
# AWS Cognito設定
# ========================================
# Cognito User PoolのJWK Set URL
COGNITO_JWK_SET_URL=https://cognito-idp.ap-northeast-1.amazonaws.com/your-pool-id/.well-known/jwks.json

# ========================================
# OpenAI設定（オプション）
# ========================================
OPENAI_API_KEY=your-openai-api-key-here
```

**セキュリティのベストプラクティス**:
- パスワードは強力なものを使用してください（大文字、小文字、数字、記号を含む、最低12文字以上）
- `.env.production`ファイルはGitにコミットしないでください（`.gitignore`に追加されていることを確認）
- 本番環境のパスワードは、開発環境とは別のものを使用してください
- ファイルの権限を制限: `chmod 600 .env.production`

**Docker Composeでの使用方法**:
```bash
# 本番環境（.env.productionファイルを明示的に指定）
docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d
docker compose --env-file .env.production -f docker-compose.backend.yaml up -d
```

---

## データベース（MySQL）のセットアップ

### ステップ1: MySQLコンテナを起動

```bash
# 本番環境用の環境変数ファイルを指定してMySQLコンテナを起動
docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d

# コンテナの状態を確認
docker ps

# MySQLのログを確認（エラーがないか確認）
docker logs smart_household_mysql

# ネットワークが作成されたことを確認
docker network ls | grep smart_household_app_network
```

**初心者向けの解説**: `docker-compose.mysql.yaml`はMySQL専用のDocker Compose設定ファイルです。`restart: always`により、コンテナが停止した場合、自動的に再起動します。`127.0.0.1:3306:3306`により、MySQLをローカルホストのみにバインドし、外部から直接アクセスできなくなります（セキュリティ向上）。

### ステップ2: MySQL接続の確認

```bash
# MySQLコンテナに接続してデータベースを確認
docker exec -it smart_household_mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;"
```

`smart_household_db`が表示されていれば、データベースの作成は成功しています。

---

## バックエンド（Spring Boot）のデプロイ

### ステップ1: MySQLが起動していることを確認

**重要**: このステップを実行する前に、MySQLが既に起動していることを確認してください。

```bash
# MySQLコンテナが起動していることを確認
docker ps | grep smart_household_mysql

# MySQLが起動していない場合は、先にMySQLを起動
docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d
```

### ステップ2: バックエンドのビルドと起動

```bash
# 本番環境用の環境変数ファイルを指定してバックエンドをビルドして起動
docker compose --env-file .env.production -f docker-compose.backend.yaml up -d --build

# ビルドと起動の進行状況を確認
docker compose -f docker-compose.backend.yaml logs -f backend
```

**初心者向けの解説**: `--build`はイメージを再ビルドします。初回実行時やコードを更新した後に使用します。`logs -f`はログをリアルタイムで表示します（`Ctrl + C`で終了）。

### ステップ3: バックエンドの動作確認

```bash
# バックエンドのヘルスチェック（Spring Boot Actuatorがある場合）
curl http://localhost:8080/actuator/health

# APIエンドポイントのテスト（認証が必要な場合）
# 注意: /api/** パスはCognito認証が必要です。JWTトークンなしでは401 Unauthorizedが返ります
curl http://localhost:8080/api/expenses/months
```

**期待される動作**:
- `/actuator/health`: 認証不要なので、バックエンドが正常に起動していればHTTPレスポンス（通常は200 OK）が返ってきます。
- `/api/expenses/months`: Cognito認証が必要なため、JWTトークンなしでは`401 Unauthorized`が返ります。これは正常な動作です（認証が正しく機能している証拠）。

**エラーが発生している場合**: ログを確認してください：`docker logs smart_household_backend`

---

## フロントエンド（Next.js）のデプロイ

### ステップ1: フロントエンドの環境変数設定

```bash
# フロントエンドディレクトリに移動
cd frontend-nextjs

# 本番環境用の環境変数ファイルを作成
nano .env.production
```

以下の内容を記述します（実際の値に置き換えてください）：

```env
# バックエンドAPIのベースURL
# 本番環境では、絶対パスを使用（明示的にドメインを指定）
# 注意: OpenAPI定義に既に /api が含まれているため、ベースURLには /api を含めません
NEXT_PUBLIC_API_BASE_URL=https://your-domain.com  # 実際のドメイン名に置き換えてください

# 開発環境の場合（ローカル開発用）
# NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

**初心者向けの解説**: 
- `NEXT_PUBLIC_`: Next.jsでは、環境変数名が`NEXT_PUBLIC_`で始まる場合のみ、ブラウザ側で使用可能になります。
- **重要な注意点**: 
  - `http://localhost:8080/api`のような設定は使用できません。ブラウザはユーザーのPC上で動作するため、サーバー側の`localhost:8080`にはアクセスできません。
  - `/api`を指定してはいけません。OpenAPI定義に既に`/api`が含まれているため、`basePath`に`/api`を指定すると、実際のリクエストが`/api/api/expenses`のようになってしまいます。
- **正しい設定方法**:
  - **本番環境（絶対パス、推奨）**: `NEXT_PUBLIC_API_BASE_URL=https://your-domain.com`を使用します（実際のドメイン名に置き換えてください）
  - **開発環境**: `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`を使用します

### ステップ2: フロントエンドのビルド

```bash
# 依存関係のインストール
npm install

# OpenAPI定義からAPIクライアントを生成（重要！）
npm run generate:api

# 本番環境用にビルド
npm run build
```

**初心者向けの解説**: 
- `npm install`: `package.json`に記載されている依存関係（ライブラリ）をインストールします。
- `npm run generate:api`: OpenAPI定義ファイルからTypeScriptのAPIクライアントコードを自動生成します。このコードがないと、ビルド時にエラーが発生します。
- `npm run build`: Next.jsアプリケーションを本番環境用に最適化してビルドします。

**ビルド後の静的ファイル**: ビルドが完了すると、`frontend-nextjs/.next/static/`ディレクトリに静的ファイル（CSS、JavaScript、画像など）が生成されます。これらのファイルは、Nginxが直接配信するため、Next.jsサーバーを経由せずに高速に配信されます。

### ステップ3: PM2でNext.jsを管理

```bash
# PM2をグローバルにインストール
sudo npm install -g pm2

# Next.jsアプリケーションをPM2で起動
cd ~/SmartHouseholdAccountBook/frontend-nextjs
pm2 start npm --name "smart-household-frontend" -- start

# PM2の状態を確認
pm2 status

# システム起動時に自動的にPM2を起動するように設定
pm2 startup
# 表示されたコマンドを実行（例: sudo env PATH=$PATH:/usr/bin pm2 startup systemd -u username --hp /home/username）

# PM2の設定を保存
pm2 save
```

**初心者向けの解説**: PM2はNode.jsアプリケーションをバックグラウンドで実行し、自動再起動、ログ管理、プロセス監視などの機能を提供します。`pm2 startup`により、システム起動時にPM2を自動的に起動するように設定します。

### ステップ4: フロントエンドの動作確認

```bash
# フロントエンドが正常に起動しているか確認
curl http://localhost:3000

# PM2のログを確認
pm2 logs smart-household-frontend
```

---

## Nginxリバースプロキシの設定

### ステップ1: Nginx設定ファイルの作成

```bash
# Nginxの設定ディレクトリに移動
cd /etc/nginx/sites-available

# 新しい設定ファイルを作成
sudo nano smart-household-account-book
```

**重要**: 初回設定時は、まず基本的なHTTP設定のみを作成します。その後、SSL証明書を取得すると、Certbotが自動的に設定を更新します。以下の設定は、SSL証明書取得後の最終的な設定例です（実際のドメイン名とユーザー名に合わせて変更してください）：

```nginx
# HTTPからHTTPSへのリダイレクト（Certbotが自動生成）
server {
    if ($host = www.your-domain.com) {  # 実際のドメイン名に置き換えてください
        return 301 https://$host$request_uri;
    } # managed by Certbot

    if ($host = your-domain.com) {  # 実際のドメイン名に置き換えてください
        return 301 https://$host$request_uri;
    } # managed by Certbot

    listen 80;
    server_name your-domain.com www.your-domain.com;  # 実際のドメイン名に置き換えてください
    return 404; # managed by Certbot
}

# HTTPSサーバー（SSL証明書設定済み）
server {
    # ポート443でSSLとHTTP/2を有効化（listenはserverブロックの最初に配置）
    listen 443 ssl http2; # managed by Certbot
    server_name your-domain.com www.your-domain.com;  # 実際のドメイン名に置き換えてください

    # SSL証明書のパス（Certbotが自動設定）
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem; # managed by Certbot（実際のドメイン名に置き換えてください）
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem; # managed by Certbot（実際のドメイン名に置き換えてください）
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

    # セキュリティヘッダー（HSTSを追加）
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # ログ設定
    access_log /var/log/nginx/smart-household-access.log;
    error_log /var/log/nginx/smart-household-error.log;

    # クライアントの最大ボディサイズ（ファイルアップロード用）
    client_max_body_size 10M;

    # 静的ファイルをNginxで直接配信（高速・パフォーマンス最適化）
    # Next.jsのビルド時に生成される静的ファイル（CSS、JavaScript、画像など）
    # 注意: /home/ubuntu の部分を、実際のユーザー名に置き換えてください
    location /_next/static {
        alias /home/ubuntu/SmartHouseholdAccountBook/frontend-nextjs/.next/static;
        expires 1y;  # 1年間ブラウザにキャッシュ
        add_header Cache-Control "public, immutable";
        access_log off;  # 静的ファイルのアクセスログは不要（ログファイルの肥大化を防ぐ）
    }

    # APIリクエストをバックエンド（Spring Boot）にプロキシ
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;

        # 認証ヘッダーを明示的に転送
        proxy_set_header Authorization $http_authorization;

        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # タイムアウト設定
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # その他のリクエストはNext.jsサーバーにプロキシ（SSR/動的ページ用）
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

**初心者向けの解説**: 
- **HTTPからHTTPSへのリダイレクト**: HTTP（ポート80）でアクセスされた場合、自動的にHTTPS（ポート443）にリダイレクトします。セキュリティのため、すべての通信を暗号化します。
- **SSL証明書**: Certbotが自動的に設定したSSL証明書のパスが記載されています。これにより、HTTPS通信が有効になります。
- **セキュリティヘッダー**: HSTS（Strict-Transport-Security）により、ブラウザにHTTPS接続を強制します。その他のセキュリティヘッダーも追加されています。
- `location /_next/static`: Next.jsのビルド時に生成される静的ファイルをNginxが直接配信します。これにより、Next.jsサーバーを経由せずに高速に配信できます。
- `location /api`: `/api`で始まるリクエストをバックエンド（Spring Boot、ポート8080）に転送します。`proxy_set_header Authorization $http_authorization;`により、JWTトークンなどの認証情報をバックエンドに正しく渡します。
- `location /`: その他のリクエスト（動的ページ、SSRが必要なページなど）をフロントエンド（Next.js、ポート3000）に転送します。

**この構成の利点**: セキュリティ強化（HTTPS、HSTS）、パフォーマンス向上（HTTP/2、静的ファイルの直接配信）、リソース節約、キャッシュ最適化

### ステップ2: 設定ファイルを有効化

```bash
# シンボリックリンクを作成して設定を有効化
sudo ln -s /etc/nginx/sites-available/smart-household-account-book /etc/nginx/sites-enabled/

# デフォルトの設定を無効化（オプション）
sudo rm /etc/nginx/sites-enabled/default

# Nginxの設定をテスト
sudo nginx -t

# エラーがなければ、Nginxを再起動
sudo systemctl restart nginx

# Nginxの状態を確認
sudo systemctl status nginx
```

### ステップ3: 動作確認

```bash
# ブラウザで http://your-domain.com にアクセスして、アプリケーションが表示されるか確認（実際のドメイン名に置き換えてください）
# または、curlで確認
curl http://your-domain.com  # 実際のドメイン名に置き換えてください
```

**注意**: この時点ではHTTP（ポート80）でのみアクセス可能です。SSL証明書を設定すると、HTTPS（ポート443）でアクセスできるようになります。

---

## SSL証明書の設定（Let's Encrypt）

### ステップ1: SSL証明書の取得

```bash
# CertbotでSSL証明書を取得（ドメイン名を実際のドメインに置き換えてください）
sudo certbot --nginx -d your-domain.com -d www.your-domain.com  # 実際のドメイン名に置き換えてください

# 対話形式で設定が進みます：
# - メールアドレスを入力（証明書の有効期限通知用）
# - 利用規約に同意
# - HTTPからHTTPSへのリダイレクトを有効化するか選択（推奨: Yes）
```

**初心者向けの解説**: Let's Encryptは無料でSSL証明書を提供する認証局です。証明書の有効期限は90日間です。Certbotは自動的に更新してくれます。

**重要**: Certbotは自動的にNginxの設定ファイルを更新します。HTTPからHTTPSへのリダイレクト設定や、SSL証明書のパスなどが自動的に追加されます。上記の設定ファイル例は、Certbot実行後の最終的な設定です。

### ステップ2: 自動更新の確認

```bash
# Certbotの自動更新をテスト
sudo certbot renew --dry-run

# 自動更新が設定されているか確認
sudo systemctl status certbot.timer
```

**初心者向けの解説**: `--dry-run`は実際には更新せず、更新プロセスが正常に動作するかテストします。Certbotは、システムのタイマー（systemd timer）を使用して、証明書を自動的に更新します。

### ステップ3: 設定の確認と改善（オプション）

Certbotが自動的に設定を追加しますが、設定ファイルを確認して、必要に応じて以下の改善を追加できます：

1. **HTTP/2の有効化**: `listen 443 ssl;`が`listen 443 ssl http2;`になっているか確認（パフォーマンス向上）
2. **HSTSヘッダーの追加**: HTTPSサーバーブロックに以下が追加されているか確認：
   ```nginx
   add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
   ```
3. **認証ヘッダーの転送**: `location /api`ブロックに以下が追加されているか確認：
   ```nginx
   proxy_set_header Authorization $http_authorization;
   ```

設定変更後の確認：

```bash
# 設定ファイルの構文チェック
sudo nginx -t

# 問題がなければNginxをリロード
sudo systemctl reload nginx
```

**初心者向けの解説**: HTTP/2はより高速な通信プロトコルです。HSTS（Strict-Transport-Security）はブラウザにHTTPS接続を強制する指示を送ります。認証ヘッダーの転送は、JWTトークンなどの認証情報をバックエンドに正しく渡すために重要です。

---

## コードの更新と再デプロイ

### ステップ1: リモートリポジトリから最新の変更を取得

```bash
# プロジェクトディレクトリに移動
cd ~/SmartHouseholdAccountBook

# リモートリポジトリから最新の変更を取得
git pull origin main
# または、ブランチ名がmasterの場合
# git pull origin master
```

### ステップ2: アプリケーションの再ビルドと再起動

```bash
# バックエンドを再ビルドして再起動
docker compose --env-file .env.production -f docker-compose.backend.yaml up -d --build

# フロントエンドディレクトリに移動してビルド
cd frontend-nextjs
npm install  # 依存関係が変更された場合
npm run generate:api  # OpenAPI定義が変更された場合、または初回
npm run build

# フロントエンドを再起動
pm2 restart smart-household-frontend
```

**初心者向けの解説**: 
- `--build`: Dockerイメージを再ビルドします。コードを変更した場合は必須です。
- `npm run generate:api`: OpenAPI定義が変更された場合、または初回デプロイ時は必須です。APIクライアントコードを再生成します。
- `npm run build`: Next.jsアプリケーションを本番環境用にビルドします。コードを変更した場合は必須です。

---

## セキュリティ設定

### ステップ1: 自動セキュリティアップデートの有効化

```bash
# 自動セキュリティアップデートをインストール
sudo apt install -y unattended-upgrades

# 設定を確認
sudo nano /etc/apt/apt.conf.d/50unattended-upgrades
```

### ステップ2: SSHのセキュリティ強化

```bash
# SSH設定ファイルを編集
sudo nano /etc/ssh/sshd_config
```

以下の設定を推奨します：

```
# パスワード認証を無効化（公開鍵認証のみ）
PasswordAuthentication no

# ルートログインを無効化
PermitRootLogin no

# SSHポートを変更（オプション、22以外のポートを使用）
# Port 2222
```

設定を変更した後：

```bash
# SSH設定をテスト
sudo sshd -t

# エラーがなければ、SSHサービスを再起動
sudo systemctl restart sshd
```

**重要**: パスワード認証を無効化する前に、公開鍵認証が正しく設定されていることを確認してください。そうでないと、サーバーに接続できなくなる可能性があります。

### ステップ3: ログの監視

```bash
# 失敗したログイン試行を監視
sudo grep "Failed password" /var/log/auth.log

# 不審なアクセスを監視
sudo tail -f /var/log/nginx/smart-household-error.log
```

---

## 監視とログ管理

### PM2の監視機能

```bash
# PM2のダッシュボードを表示
pm2 monit

# アプリケーションの詳細情報を表示
pm2 show smart-household-frontend

# ログをリアルタイムで表示
pm2 logs smart-household-frontend
```

### Dockerコンテナの監視

```bash
# 実行中のコンテナの状態を確認
docker ps

# コンテナのリソース使用状況を確認
docker stats

# コンテナのログを確認
docker logs -f smart_household_backend
docker logs -f smart_household_mysql
```

### Nginxのログ確認

```bash
# アクセスログを確認
sudo tail -f /var/log/nginx/smart-household-access.log

# エラーログを確認
sudo tail -f /var/log/nginx/smart-household-error.log
```

### システムリソースの監視

```bash
# CPUとメモリの使用状況を確認
htop
# または
top

# ディスク使用量を確認
df -h

# メモリ使用量を確認
free -h
```

---

## トラブルシューティング

### よくある問題と解決方法

#### 1. バックエンドが起動しない

**症状**: `docker logs smart_household_backend`でエラーが表示される

**確認事項**:
```bash
# MySQLが正常に起動しているか確認
docker ps | grep mysql

# MySQLのログを確認
docker logs smart_household_mysql

# 環境変数が正しく設定されているか確認
cat .env.production | grep SPRING_DATASOURCE_URL
```

**解決方法**:
- MySQLが起動していない場合: `docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d`
- 環境変数が間違っている場合: `.env.production`ファイルを確認して修正

#### 2. フロントエンドが表示されない

**症状**: ブラウザでアクセスしてもページが表示されない

**確認事項**:
```bash
# PM2の状態を確認
pm2 status

# Next.jsのログを確認
pm2 logs smart-household-frontend

# ポート3000がリッスンされているか確認
netstat -tlnp | grep 3000
```

**解決方法**:
- PM2が停止している場合: `pm2 restart smart-household-frontend`
- ポートが使用されていない場合: `pm2 start npm --name "smart-household-frontend" -- start`

#### 3. フロントエンドのビルドエラー

**よくあるエラー1: `Module not found: Can't resolve './generated/configuration'`**

**原因**: OpenAPI GeneratorでAPIクライアントコードが生成されていない

**解決方法**:
```bash
cd ~/SmartHouseholdAccountBook/frontend-nextjs
npm run generate:api
npm run build
```

**よくあるエラー2: `Error: /bin/sh: 1: java: not found`**

**原因**: Javaがインストールされていない

**解決方法**:
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
cd ~/SmartHouseholdAccountBook/frontend-nextjs
npm run generate:api
npm run build
```

**よくあるエラー3: `Could not find a production build in the '.next' directory`**

**原因**: PM2で`npm start`を実行する前に`npm run build`を実行していない

**解決方法**:
```bash
cd ~/SmartHouseholdAccountBook/frontend-nextjs
npm run build
pm2 restart smart-household-frontend
```

#### 4. Nginxが502エラーを返す

**症状**: ブラウザで502 Bad Gatewayエラーが表示される

**確認事項**:
```bash
# Nginxのエラーログを確認
sudo tail -f /var/log/nginx/smart-household-error.log

# バックエンドとフロントエンドが起動しているか確認
docker ps | grep backend
pm2 status
```

**解決方法**:
- バックエンドまたはフロントエンドが停止している場合、再起動
- Nginxの設定ファイルに構文エラーがないか確認: `sudo nginx -t`

#### 5. SSL証明書の更新に失敗する

**症状**: Certbotの自動更新が失敗する

**確認事項**:
```bash
# Certbotのログを確認
sudo tail -f /var/log/letsencrypt/letsencrypt.log

# 証明書の有効期限を確認
sudo certbot certificates
```

**解決方法**:
- ドメインのDNS設定が正しいか確認
- ファイアウォールでポート80と443が開放されているか確認
- 手動で更新を試行: `sudo certbot renew`

#### 6. データベース接続エラー

**症状**: バックエンドがデータベースに接続できない

**確認事項**:
```bash
# MySQLコンテナが起動しているか確認
docker ps | grep mysql

# MySQLに直接接続してテスト
docker exec -it smart_household_mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1;"
```

**解決方法**:
- MySQLコンテナが停止している場合: `docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d`
- 接続URLが間違っている場合: `.env.production`ファイルの`SPRING_DATASOURCE_URL_PROD`を確認

### ログファイルの場所

| サービス | ログファイルの場所 |
|---------|-----------------|
| Next.js (PM2) | `~/.pm2/logs/` |
| Spring Boot (Docker) | `docker logs smart_household_backend` |
| MySQL (Docker) | `docker logs smart_household_mysql` |
| Nginx | `/var/log/nginx/smart-household-*.log` |
| システムログ | `/var/log/syslog` |

---

## デプロイ後の確認チェックリスト

デプロイが完了したら、以下の項目を確認してください：

- [ ] ブラウザでアプリケーションにアクセスできる
- [ ] HTTPSが正しく動作している（SSL証明書が有効）
- [ ] ログイン機能が動作する（AWS Cognito認証）
- [ ] 支出の追加・編集・削除が動作する
- [ ] データベースにデータが正しく保存される
- [ ] ログにエラーが表示されていない
- [ ] システムリソース（CPU、メモリ）が適切な範囲内
- [ ] 自動更新（セキュリティパッチ、SSL証明書）が設定されている

---

## まとめ

このガイドでは、さくらのVPSのUbuntuサーバーにSmart Household Account Bookプロジェクトをデプロイする手順を詳しく説明しました。

### 主なポイント

1. **セキュリティ**: ファイアウォール、SSL証明書、環境変数の適切な管理
2. **パフォーマンス**: Nginxリバースプロキシによる効率的なリクエスト処理
3. **可用性**: PM2とDocker Composeによる自動再起動、ヘルスチェック
4. **監視**: ログとリソース使用状況の定期的な確認

### 次のステップ

- 定期的なバックアップの設定（データベース、環境変数）
- 監視ツールの導入（例: Prometheus、Grafana）
- CI/CDパイプラインの構築（GitHub Actionsなど）
- パフォーマンスチューニング（キャッシュ、CDNの導入など）

### 参考資料

- [Docker公式ドキュメント](https://docs.docker.com/)
- [Next.js公式ドキュメント](https://nextjs.org/docs)
- [Spring Boot公式ドキュメント](https://spring.io/projects/spring-boot)
- [Nginx公式ドキュメント](https://nginx.org/en/docs/)
- [Let's Encrypt公式ドキュメント](https://letsencrypt.org/docs/)

---

**質問や問題が発生した場合は、ログを確認し、このガイドのトラブルシューティングセクションを参照してください。**
