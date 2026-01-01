# さくらのVPS Ubuntuサーバーへのデプロイガイド

> このドキュメントは、Smart Household Account BookプロジェクトをさくらのVPSのUbuntuサーバーにデプロイするための初心者向けガイドです。各ステップを詳しく解説し、セキュリティとパフォーマンスを重視した構成を説明します。

## 📋 目次

1. [デプロイ構成の概要](#デプロイ構成の概要)
2. [前提条件](#前提条件)
3. [サーバーの初期セットアップ](#サーバーの初期セットアップ)
4. [必要なソフトウェアのインストール](#必要なソフトウェアのインストール)
5. [プロジェクトのデプロイ準備](#プロジェクトのデプロイ準備)
6. [データベース（MySQL）のセットアップ](#データベースmysqlのセットアップ)
7. [バックエンド（Spring Boot）のデプロイ](#バックエンドspring-bootのデプロイ)
8. [フロントエンド（Next.js）のデプロイ](#フロントエンドnextjsのデプロイ)
9. [Nginxリバースプロキシの設定](#nginxリバースプロキシの設定)
10. [SSL証明書の設定（Let's Encrypt）](#ssl証明書の設定lets-encrypt)
11. [環境変数の管理](#環境変数の管理)
12. [セキュリティ設定](#セキュリティ設定)
13. [監視とログ管理](#監視とログ管理)
14. [トラブルシューティング](#トラブルシューティング)

---

## デプロイ構成の概要

### アーキテクチャ図

```
┌─────────────────────────────────────────────────────────┐
│                    インターネット                         │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ HTTPS (443)
                       │ HTTP (80) → HTTPS リダイレクト
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  Nginx (リバースプロキシ)                 │
│  - SSL/TLS終端                                          │
│  - 静的ファイル配信（Next.js）                           │
│  - APIリクエストのプロキシ（Spring Boot）                │
└──────┬──────────────────────────────┬───────────────────┘
       │                              │
       │ /api/*                       │ /
       ▼                              ▼
┌──────────────────┐         ┌──────────────────┐
│  Spring Boot      │         │   Next.js        │
│  (ポート: 8080)   │         │  (ポート: 3000)  │
│                   │         │                  │
│  - REST API       │         │  - React UI      │
│  - JWT認証        │         │  - SSR/SSG       │
└────────┬─────────┘         └──────────────────┘
         │
         │ JDBC
         ▼
┌──────────────────┐
│   MySQL 8.0      │
│  (ポート: 3306)  │
│                  │
│  - データ保存     │
└──────────────────┘
```

### 構成の説明

**初心者向けの解説**:
- **Nginx**: Webサーバーとリバースプロキシの役割を担います。外部からのリクエストを受け取り、適切なアプリケーションに振り分けます。また、Next.jsの静的ファイル（CSS、JavaScript、画像など）を直接配信することで、パフォーマンスを最適化します。
- **Next.js**: フロントエンドアプリケーション。ユーザーがブラウザで見る画面を提供します。静的ファイルはNginxが直接配信し、動的ページ（SSRが必要なページ）のみNext.jsサーバーで処理されます。
- **Spring Boot**: バックエンドAPIサーバー。データベースとのやり取りやビジネスロジックを処理します。
- **MySQL**: データベース。支出データなどの情報を保存します。

**リクエストの振り分け**:
- `/_next/static/*`: 静的ファイル（CSS、JavaScript、画像など）→ Nginxがファイルシステムから直接配信（高速）
- `/api/*`: APIリクエスト → Spring Bootにプロキシ
- その他（`/`）: 動的ページ（SSRが必要なページ）→ Next.jsサーバーにプロキシ

**Docker Composeファイルの構成**:
本番環境では、MySQLとSpring Bootを**別々のDocker Composeファイル**で管理します：
- **`docker-compose.mysql.yaml`**: MySQLのみを起動（データベースの独立性を確保）
- **`docker-compose.backend.yaml`**: Spring Bootのみを起動（既存のMySQLコンテナに接続）

**なぜ分離するのか？**
- **独立性**: MySQLとSpring Bootを独立して管理・更新できる
- **リソース管理**: それぞれに最適なリソースを割り当てられる
- **スケーラビリティ**: 個別にスケールできる
- **セキュリティ**: データベースをより厳重に保護できる

### ポート構成

| サービス | 内部ポート | 外部公開 | 説明 |
|---------|----------|---------|------|
| Nginx | 80, 443 | ✅ | HTTP/HTTPSアクセス |
| Next.js | 3000 | ❌ | 内部のみ（Nginx経由） |
| Spring Boot | 8080 | ❌ | 内部のみ（Nginx経由） |
| MySQL | 3306 | ❌ | 内部のみ（セキュリティのため） |

**なぜ内部ポートを外部公開しないのか？**
- セキュリティを向上させるため。Nginxが唯一の入口となり、ファイアウォールで他のポートを閉じることができます。
- 直接アクセスを防ぐことで、不正なリクエストをブロックできます。

---

## 前提条件

### 必要なもの

1. **さくらのVPSサーバー**
   - OS: Ubuntu 22.04 LTS 以上（推奨: Ubuntu 22.04 または 24.04）
   - メモリ: 最低2GB（推奨: 4GB以上）
   - ストレージ: 最低20GB（推奨: 40GB以上）
   - ルート権限（sudo）を持つユーザーアカウント

2. **ドメイン名**（オプション、推奨）
   - 例: `smart-household-account-book.com`
   - SSL証明書を取得するために必要

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

ローカルマシンからVPSサーバーにSSH接続します。

```bash
# サーバーにSSH接続（IPアドレスとユーザー名を置き換えてください）
ssh username@your-server-ip

# 例: ssh root@123.456.789.012
```

**初心者向けの解説**:
- **SSH**: Secure Shellの略。サーバーに安全に接続するためのプロトコルです。
- 初回接続時は、サーバーのフィンガープリントを確認するメッセージが表示されます。`yes`と入力して続行します。

### ステップ2: システムの更新

サーバーのパッケージを最新の状態に更新します。

```bash
# パッケージリストの更新
sudo apt update

# インストール済みパッケージのアップグレード
sudo apt upgrade -y

# システムの再起動（必要に応じて）
sudo reboot
```

**初心者向けの解説**:
- `apt update`: パッケージリストを最新の状態に更新します。これは「何がインストール可能か」を確認する作業です。
- `apt upgrade`: 既にインストールされているパッケージを最新版に更新します。セキュリティパッチが含まれることが多いため、重要です。
- `-y`: 確認メッセージをスキップして自動的に「yes」と答えます。

### ステップ3: ファイアウォールの設定

Ubuntuのファイアウォール（UFW）を設定して、必要なポートのみを開放します。

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

**初心者向けの解説**:
- **UFW**: Uncomplicated Firewallの略。Linuxのファイアウォールを簡単に設定できるツールです。
- **ポート**: ネットワーク通信の入口となる番号です。HTTPは80、HTTPSは443、SSHは22が標準です。
- **重要**: SSH（22番ポート）を最初に許可しないと、次回接続できなくなる可能性があります。

---

## 必要なソフトウェアのインストール

### ステップ1: DockerとDocker Composeのインストール

Dockerは、アプリケーションをコンテナとして実行するためのプラットフォームです。Docker Composeは、複数のコンテナを管理するためのツールです。

```bash
# 必要なパッケージのインストール
sudo apt install -y ca-certificates curl gnupg lsb-release
# ca-certificates: SSL/TLS証明書の検証用
# curl: HTTP/HTTPSでファイルをダウンロードするツール
# gnupg: GPGキーを扱うツール（パッケージの署名検証用）
# lsb-release: Linuxディストリビューション情報を取得するツール

# Dockerの公式GPGキーを追加
sudo mkdir -p /etc/apt/keyrings
# GPGキーを保存するディレクトリを作成（-p: 既存でもエラーにしない）

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
# Docker公式のGPG公開鍵をダウンロードして、バイナリ形式に変換して保存
# -fsSL: エラー時に失敗、進捗非表示、リダイレクト追従
# --dearmor: テキスト形式のGPGキーをバイナリ形式に変換

# Dockerのリポジトリを追加
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
# Docker公式リポジトリの設定を追加
# dpkg --print-architecture: システムのアーキテクチャ（例: amd64）を取得
# lsb_release -cs: Ubuntuのコード名（例: jammy）を取得
# stable: 安定版チャンネルを指定

# パッケージリストを更新
sudo apt update
# 追加したリポジトリを含む全リポジトリから最新のパッケージ情報を取得

# Docker EngineとDocker Composeをインストール
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
# docker-ce: Docker Community Edition（無料版）
# docker-ce-cli: Dockerコマンドラインインターフェース
# containerd.io: コンテナランタイム（コンテナの実行を管理）
# docker-buildx-plugin: マルチプラットフォーム対応のビルド機能
# docker-compose-plugin: Docker Compose（複数コンテナの管理）

# Dockerサービスを起動
sudo systemctl start docker
# Dockerサービスを起動

sudo systemctl enable docker
# システム起動時に自動的にDockerサービスを開始するように設定

# 現在のユーザーをdockerグループに追加（sudoなしでDockerを使えるようにする）
sudo usermod -aG docker $USER
# 現在のユーザーをdockerグループに追加
# -aG: 既存グループを維持したまま、dockerグループを追加
# $USER: 現在のユーザー名

# インストールの確認
docker --version
# Dockerのバージョンを表示

docker compose version
# Docker Composeのバージョンを表示
```

**初心者向けの解説**:
- **Docker**: アプリケーションとその依存関係を「コンテナ」という単位でパッケージ化する技術です。これにより、どの環境でも同じように動作させることができます。
- **Docker Compose**: 複数のコンテナ（例: アプリケーションとデータベース）を一度に管理するためのツールです。
- **GPGキー**: パッケージの信頼性を確認するための暗号化キーです。Docker公式のパッケージであることを検証するために使用します。
- **リポジトリ**: パッケージをダウンロードする元となる場所です。Docker公式リポジトリを追加することで、最新版のDockerをインストールできます。
- **systemctl enable**: システム起動時に自動的にサービスを開始するように設定します。

**重要**: ユーザーをdockerグループに追加した後、一度ログアウトして再ログインする必要があります。

```bash
# ログアウト
exit

# 再度SSH接続
ssh username@your-server-ip
```

### ステップ2: Node.jsとnpmのインストール

Next.jsアプリケーションをビルド・実行するために、Node.jsが必要です。

```bash
# Node.js 20.x（LTS版）をインストール
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# インストールの確認
node --version
npm --version
```

**初心者向けの解説**:
- **Node.js**: JavaScriptをサーバー側で実行するためのランタイムです。Next.jsはNode.js上で動作します。
- **npm**: Node Package Managerの略。Node.jsのパッケージ（ライブラリ）を管理するツールです。
- **LTS**: Long Term Supportの略。長期サポート版で、安定性が重視されます。

### ステップ3: Nginxのインストール

Nginxは、Webサーバーとリバースプロキシとして機能します。

```bash
# Nginxのインストール
sudo apt install -y nginx

# Nginxサービスを起動
sudo systemctl start nginx
sudo systemctl enable nginx

# インストールの確認
nginx -v
```

**初心者向けの解説**:
- **Nginx**: 高性能なWebサーバーです。静的ファイルの配信や、リクエストを別のサーバーに転送（リバースプロキシ）する役割を担います。
- **リバースプロキシ**: クライアントからのリクエストを受け取り、適切なバックエンドサーバーに転送する仕組みです。

### ステップ4: Certbotのインストール（SSL証明書用）

Let's Encryptから無料のSSL証明書を取得するために、Certbotをインストールします。

```bash
# Certbotのインストール
sudo apt install -y certbot python3-certbot-nginx
```

**初心者向けの解説**:
- **Certbot**: Let's EncryptからSSL証明書を自動的に取得・更新するツールです。
- **SSL証明書**: HTTPS通信を有効にするために必要な証明書です。データの暗号化を可能にします。

---

## プロジェクトのデプロイ準備

### ステップ1: プロジェクトのクローン

GitHubなどのリポジトリからプロジェクトをクローンします。

```bash
# ホームディレクトリに移動
cd ~

# プロジェクトをクローン（URLを実際のリポジトリURLに置き換えてください）
git clone https://github.com/your-username/SmartHouseholdAccountBook.git

# プロジェクトディレクトリに移動
cd SmartHouseholdAccountBook
```

**初心者向けの解説**:
- **Git clone**: リモートリポジトリ（GitHubなど）からプロジェクトのコードをダウンロードするコマンドです。
- プライベートリポジトリの場合は、認証情報が必要になることがあります。

### ステップ2: 環境変数ファイルの作成

**重要**: 開発環境と本番環境で環境変数を分離することを強く推奨します。

本番環境用の環境変数ファイルを作成します：

```bash
# プロジェクトルートに本番環境用の.env.productionファイルを作成
nano .env.production
```

以下の内容を記述します（実際の値はローカルPCの.envの値を利用）：

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
# 形式: jdbc:mysql://mysql:3306/データベース名?useSSL=true&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_URL_PROD=jdbc:mysql://mysql:3306/smart_household_db?useSSL=true&serverTimezone=Asia/Tokyo

# ========================================
# AWS Cognito設定
# ========================================
# Cognito User PoolのJWK Set URL
# 形式: https://cognito-idp.{リージョン}.amazonaws.com/{UserPoolId}/.well-known/jwks.json
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

**環境変数ファイルの使い分け**:
- **開発環境**: `.env`ファイルを使用（ローカル開発用）
- **本番環境**: `.env.production`ファイルを使用（サーバー上で使用）

**Docker Composeでの使用方法**:
```bash
# 開発環境（.envファイルが自動的に使用される）
docker compose -f docker-compose.dev.yaml up -d

# 本番環境（.env.productionファイルを明示的に指定）
docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d
docker compose --env-file .env.production -f docker-compose.backend.yaml up -d
```

**ファイルを保存**:
- `nano`エディタで編集後、`Ctrl + O`で保存、`Enter`で確定、`Ctrl + X`で終了します。

---

## データベース（MySQL）のセットアップ

### ステップ1: Docker ComposeでMySQLを起動

プロジェクトには`docker-compose.yaml`が含まれていますが、本番環境ではMySQLのみをDockerで実行し、アプリケーションは別途デプロイします。

まず、MySQL専用の`docker-compose.mysql.yaml`を作成します：

```bash
# MySQL専用のdocker-composeファイルを作成
nano docker-compose.mysql.yaml
```

以下の内容を記述します：

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: smart_household_mysql
    restart: always
    ports:
      - "127.0.0.1:3306:3306"  # ローカルホストのみにバインド（セキュリティ）
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./docker/mysql/my.cnf:/etc/mysql/conf.d/my.cnf
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

volumes:
  mysql_data:
    name: smart_household_mysql_data

networks:
  app-network:
    name: smart_household_app_network
    driver: bridge
```

**初心者向けの解説**:
- `restart: always`: コンテナが停止した場合、自動的に再起動します。
- `127.0.0.1:3306:3306`: MySQLをローカルホスト（127.0.0.1）のみにバインドします。これにより、外部から直接アクセスできなくなります（セキュリティ向上）。
- `volumes`: データの永続化のため、MySQLのデータをホストマシンのボリュームに保存します。
- `networks`: `smart_household_app_network`という名前のネットワークを作成します。このネットワークは、後で起動するSpring Bootコンテナと共有されます。

### ステップ2: MySQLコンテナを起動

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

**初心者向けの解説**:
- `-f docker-compose.mysql.yaml`: 使用するdocker-composeファイルを指定します。
- `-d`: バックグラウンドで実行します（デタッチモード）。
- `docker ps`: 実行中のコンテナ一覧を表示します。
- `docker logs`: コンテナのログを確認します。エラーが発生していないか確認するために使用します。

### ステップ3: MySQL接続の確認

```bash
# MySQLコンテナに接続してデータベースを確認
docker exec -it smart_household_mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;"
```

**期待される出力**:
```
+-------------------------+
| Database                |
+-------------------------+
| information_schema      |
| mysql                   |
| performance_schema      |
| smart_household_db      |
| sys                     |
+-------------------------+
```

`smart_household_db`が表示されていれば、データベースの作成は成功しています。

---

## バックエンド（Spring Boot）のデプロイ

### ステップ1: バックエンド用のDocker Composeファイルを作成

**重要**: このステップを実行する前に、MySQLが既に起動していることを確認してください（前のセクション「データベース（MySQL）のセットアップ」を参照）。

Spring Boot専用の`docker-compose.backend.yaml`を作成します：

```bash
nano docker-compose.backend.yaml
```

以下の内容を記述します：

```yaml
services:
  backend:
    container_name: smart_household_backend
    build:
      context: ./
      dockerfile: backend/Dockerfile
    restart: always
    ports:
      - "127.0.0.1:8080:8080"  # ローカルホストのみにバインド
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL_PROD}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_ROOT_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_JPA_SHOW_SQL: "false"  # 本番環境ではSQLログを無効化
      COGNITO_JWK_SET_URL: ${COGNITO_JWK_SET_URL}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
    networks:
      - app-network

networks:
  app-network:
    name: smart_household_app_network
    external: true  # 既存のネットワーク（MySQLで作成済み）を使用
```

**初心者向けの解説**:
- **`networks`**: コンテナ間の通信を管理するネットワークです。`smart_household_app_network`という名前のネットワークは、MySQLコンテナ起動時に既に作成されています。
- **`external: true`**: 既存のネットワークを使用することを示します。これにより、別のdocker-composeファイルで起動したMySQLコンテナと通信できます。
- **MySQLとの接続**: Spring Bootコンテナは、同じネットワーク（`smart_household_app_network`）上にあるMySQLコンテナに、ホスト名`smart_household_mysql`で接続できます。
- **`depends_on`は不要**: MySQLは別のdocker-composeファイルで管理されているため、`depends_on`は使用しません。代わりに、MySQLが起動していることを手動で確認してから、Spring Bootを起動します。

### ステップ2: MySQLが起動していることを確認

```bash
# MySQLコンテナが起動していることを確認
docker ps | grep smart_household_mysql

# MySQLが起動していない場合は、先にMySQLを起動（本番環境用の環境変数ファイルを指定）
docker compose --env-file .env.production -f docker-compose.mysql.yaml up -d
```

**初心者向けの解説**:
- バックエンドを起動する前に、MySQLが既に起動している必要があります。
- `docker ps`: 実行中のコンテナ一覧を表示します。`smart_household_mysql`が表示されていれば、MySQLは起動しています。

### ステップ3: バックエンドのビルドと起動

```bash
# 本番環境用の環境変数ファイルを指定してバックエンドをビルドして起動
docker compose --env-file .env.production -f docker-compose.backend.yaml up -d --build

# ビルドと起動の進行状況を確認
docker compose -f docker-compose.backend.yaml logs -f backend
```

**初心者向けの解説**:
- `--build`: イメージを再ビルドします。初回実行時やコードを更新した後に使用します。
- `-f docker-compose.backend.yaml`: Spring Boot専用のdocker-composeファイルを指定します。
- `logs -f`: ログをリアルタイムで表示します（`Ctrl + C`で終了）。

### ステップ3: バックエンドの動作確認

```bash
# バックエンドのヘルスチェック（Spring Boot Actuatorがある場合）
curl http://localhost:8080/actuator/health

# または、簡単なAPIエンドポイントをテスト
curl http://localhost:8080/api/expenses/months
```

**期待される動作**:
- バックエンドが正常に起動していれば、HTTPレスポンスが返ってきます。
- エラーが発生している場合は、ログを確認してください：`docker logs smart_household_backend`

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
# バックエンドAPIのURL
# 本番環境では、Nginx経由でアクセスするため、内部URLを使用
NEXT_PUBLIC_API_URL=http://localhost:8080/api

# または、ドメインを使用する場合（Nginx経由）
# NEXT_PUBLIC_API_URL=https://your-domain.com/api
```

**初心者向けの解説**:
- `NEXT_PUBLIC_`: Next.jsでは、環境変数名が`NEXT_PUBLIC_`で始まる場合のみ、ブラウザ側で使用可能になります。
- 本番環境では、Nginxがリバースプロキシとして機能するため、APIリクエストは`/api`パスでNginxに送信され、Nginxがバックエンドに転送します。

### ステップ2: フロントエンドのビルド

```bash
# 依存関係のインストール
npm install

# 本番環境用にビルド
npm run build
```

**初心者向けの解説**:
- `npm install`: `package.json`に記載されている依存関係（ライブラリ）をインストールします。
- `npm run build`: Next.jsアプリケーションを本番環境用に最適化してビルドします。TypeScriptのコンパイル、コードの最適化、静的ページの生成などが行われます。

**ビルド後の静的ファイル**:
- ビルドが完了すると、`frontend-nextjs/.next/static/`ディレクトリに静的ファイル（CSS、JavaScript、画像など）が生成されます。
- これらのファイルは、Nginxが直接配信するため、Next.jsサーバーを経由せずに高速に配信されます。
- 静的ファイルのパスは、Nginx設定ファイルの`location /_next/static`ブロックで指定します。

### ステップ3: PM2でNext.jsを管理

PM2は、Node.jsアプリケーションを本番環境で実行・管理するためのプロセスマネージャーです。

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

**初心者向けの解説**:
- **PM2**: Node.jsアプリケーションをバックグラウンドで実行し、自動再起動、ログ管理、プロセス監視などの機能を提供します。
- `pm2 startup`: システム起動時にPM2を自動的に起動するように設定します。サーバーが再起動されても、アプリケーションが自動的に起動します。
- `pm2 save`: 現在のPM2の設定を保存します。

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

# 新しい設定ファイルを作成（ドメイン名を置き換えてください）
sudo nano smart-household-account-book
```

以下の内容を記述します（`your-domain.com`を実際のドメイン名に置き換えてください）：

**重要**: フロントエンドのビルドが完了していることを確認してください（`npm run build`を実行済み）。静的ファイルは`frontend-nextjs/.next/static/`ディレクトリに生成されます。

```nginx
# HTTPからHTTPSへのリダイレクト（後でSSL証明書を設定した後に有効化）
# server {
#     listen 80;
#     server_name your-domain.com www.your-domain.com;
#     return 301 https://$server_name$request_uri;
# }

# HTTPサーバー（SSL証明書設定前はこちらを使用）
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    # セキュリティヘッダー
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
    # パス: frontend-nextjs/.next/static/
    # 注意: /home/username の部分を、実際のユーザー名に置き換えてください
    # 例: /home/yuki/SmartHouseholdAccountBook/frontend-nextjs/.next/static
    location /_next/static {
        alias /home/username/SmartHouseholdAccountBook/frontend-nextjs/.next/static;
        expires 1y;  # 1年間ブラウザにキャッシュ
        add_header Cache-Control "public, immutable";
        access_log off;  # 静的ファイルのアクセスログは不要（ログファイルの肥大化を防ぐ）
    }

    # APIリクエストをバックエンド（Spring Boot）にプロキシ
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
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

# HTTPSサーバー（SSL証明書設定後に有効化）
# server {
#     listen 443 ssl http2;
#     server_name your-domain.com www.your-domain.com;
#
#     # SSL証明書のパス（Certbotが自動的に設定）
#     ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
#     ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
#
#     # SSL設定（セキュリティ強化）
#     ssl_protocols TLSv1.2 TLSv1.3;
#     ssl_ciphers HIGH:!aNULL:!MD5;
#     ssl_prefer_server_ciphers on;
#
#     # セキュリティヘッダー
#     add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
#     add_header X-Frame-Options "SAMEORIGIN" always;
#     add_header X-Content-Type-Options "nosniff" always;
#     add_header X-XSS-Protection "1; mode=block" always;
#
#     # ログ設定
#     access_log /var/log/nginx/smart-household-access.log;
#     error_log /var/log/nginx/smart-household-error.log;
#
#     # クライアントの最大ボディサイズ
#     client_max_body_size 10M;
#
#     # 静的ファイルをNginxで直接配信（高速・パフォーマンス最適化）
#     # 注意: /home/username の部分を、実際のユーザー名に置き換えてください
#     location /_next/static {
#         alias /home/username/SmartHouseholdAccountBook/frontend-nextjs/.next/static;
#         expires 1y;
#         add_header Cache-Control "public, immutable";
#         access_log off;
#     }
#
#     # APIリクエストをバックエンドにプロキシ
#     location /api {
#         proxy_pass http://localhost:8080;
#         proxy_http_version 1.1;
#         proxy_set_header Upgrade $http_upgrade;
#         proxy_set_header Connection 'upgrade';
#         proxy_set_header Host $host;
#         proxy_set_header X-Real-IP $remote_addr;
#         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
#         proxy_set_header X-Forwarded-Proto $scheme;
#         proxy_cache_bypass $http_upgrade;
#         
#         proxy_connect_timeout 60s;
#         proxy_send_timeout 60s;
#         proxy_read_timeout 60s;
#     }
#
#     # その他のリクエストはNext.jsサーバーにプロキシ（SSR/動的ページ用）
#     location / {
#         proxy_pass http://localhost:3000;
#         proxy_http_version 1.1;
#         proxy_set_header Upgrade $http_upgrade;
#         proxy_set_header Connection 'upgrade';
#         proxy_set_header Host $host;
#         proxy_set_header X-Real-IP $remote_addr;
#         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
#         proxy_set_header X-Forwarded-Proto $scheme;
#         proxy_cache_bypass $http_upgrade;
#     }
# }
```

**初心者向けの解説**:
- `location /_next/static`: Next.jsのビルド時に生成される静的ファイル（CSS、JavaScript、画像など）をNginxが直接配信します。これにより、Next.jsサーバーを経由せずに高速に配信できます。
  - `alias`: ファイルシステム上の実際のパスを指定します。`/home/username`の部分は、実際のユーザー名に置き換えてください。
  - `expires 1y`: ブラウザに1年間キャッシュさせます。これにより、同じファイルを何度もダウンロードする必要がなくなります。
  - `access_log off`: 静的ファイルのアクセスログを無効化します。ログファイルの肥大化を防ぎます。
- `location /api`: `/api`で始まるリクエストをバックエンド（Spring Boot、ポート8080）に転送します。
- `location /`: その他のリクエスト（動的ページ、SSRが必要なページなど）をフロントエンド（Next.js、ポート3000）に転送します。
- `proxy_set_header`: リクエストヘッダーを設定します。これにより、バックエンドがクライアントの実際のIPアドレスなどを取得できます。
- `X-Forwarded-Proto`: クライアントがHTTPSで接続した場合、バックエンドにその情報を伝えます。

**この構成の利点**:
- **パフォーマンス向上**: 静的ファイル（CSS、JavaScript、画像など）はNginxが直接配信するため、非常に高速です。
- **リソース節約**: Next.jsサーバーの負荷が軽減され、動的ページ（SSR）の処理に集中できます。
- **キャッシュ最適化**: 静的ファイルは1年間キャッシュされるため、ブラウザの再ダウンロードが不要になります。

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

**初心者向けの解説**:
- `ln -s`: シンボリックリンク（ショートカットのようなもの）を作成します。`sites-enabled`ディレクトリ内のファイルが実際に使用される設定です。
- `nginx -t`: Nginxの設定ファイルに構文エラーがないかテストします。エラーがある場合は、修正してから再起動してください。

### ステップ3: 動作確認

```bash
# ブラウザで http://your-domain.com にアクセスして、アプリケーションが表示されるか確認
# または、curlで確認
curl http://your-domain.com

# 静的ファイルがNginxで直接配信されているか確認
# ビルド後に生成されるCSSファイルのパスを確認（実際のパスはビルド結果に依存します）
curl -I http://your-domain.com/_next/static/css/app.css

# 期待される動作:
# - HTTPステータスコード 200 が返ってくる
# - レスポンスヘッダーに "Cache-Control: public, immutable" が含まれる
# - レスポンスヘッダーに "Expires" が含まれる（1年後）

# 注意: ビルド後の実際のファイル名は異なる場合があります
# ブラウザの開発者ツール（F12）のNetworkタブで、実際の静的ファイルのパスを確認できます
```

---

## SSL証明書の設定（Let's Encrypt）

### ステップ1: SSL証明書の取得

```bash
# CertbotでSSL証明書を取得（ドメイン名を置き換えてください）
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 対話形式で設定が進みます：
# - メールアドレスを入力（証明書の有効期限通知用）
# - 利用規約に同意
# - HTTPからHTTPSへのリダイレクトを有効化するか選択（推奨: Yes）
```

**初心者向けの解説**:
- **Let's Encrypt**: 無料でSSL証明書を提供する認証局です。
- **証明書の有効期限**: 90日間です。Certbotは自動的に更新してくれます。
- **リダイレクト**: HTTP（ポート80）でアクセスされた場合、自動的にHTTPS（ポート443）にリダイレクトします。

### ステップ2: 自動更新の確認

```bash
# Certbotの自動更新をテスト
sudo certbot renew --dry-run

# 自動更新が設定されているか確認
sudo systemctl status certbot.timer
```

**初心者向けの解説**:
- `--dry-run`: 実際には更新せず、更新プロセスが正常に動作するかテストします。
- Certbotは、システムのタイマー（systemd timer）を使用して、証明書を自動的に更新します。

### ステップ3: Nginx設定の更新

Certbotが自動的にNginxの設定ファイルを更新しますが、念のため確認してください：

```bash
# 設定ファイルを確認
sudo nano /etc/nginx/sites-available/smart-household-account-book
```

SSL証明書が正しく設定され、HTTPSサーバーブロックが有効になっていることを確認します。

---

## 環境変数の管理

### セキュアな環境変数管理のベストプラクティス

1. **`.env`ファイルの権限設定**
   ```bash
   # .envファイルの権限を制限（所有者のみ読み取り可能）
   chmod 600 .env
   ```

2. **環境変数のバックアップ**
   ```bash
   # 環境変数ファイルを安全な場所にバックアップ（暗号化推奨）
   # 例: パスワードマネージャーや暗号化されたストレージに保存
   ```

3. **機密情報のローテーション**
   - 定期的にパスワードやAPIキーを変更する
   - 変更後は、アプリケーションを再起動する

### 環境変数の更新とアプリケーションの再起動

```bash
# 環境変数を更新した後、アプリケーションを再起動

# バックエンドの再起動
cd ~/SmartHouseholdAccountBook
docker compose -f docker-compose.backend.yaml restart backend

# フロントエンドの再起動
pm2 restart smart-household-frontend
```

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
cat .env | grep SPRING_DATASOURCE_URL
```

**解決方法**:
- MySQLが起動していない場合: `docker compose -f docker-compose.mysql.yaml up -d`
- 環境変数が間違っている場合: `.env`ファイルを確認して修正

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

#### 3. Nginxが502エラーを返す

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

#### 4. SSL証明書の更新に失敗する

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

#### 5. データベース接続エラー

**症状**: バックエンドがデータベースに接続できない

**確認事項**:
```bash
# MySQLコンテナが起動しているか確認
docker ps | grep mysql

# MySQLに直接接続してテスト
docker exec -it smart_household_mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1;"
```

**解決方法**:
- MySQLコンテナが停止している場合: `docker compose -f docker-compose.mysql.yaml up -d`
- 接続URLが間違っている場合: `.env`ファイルの`SPRING_DATASOURCE_URL_PROD`を確認

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

