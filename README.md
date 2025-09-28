# SmartHouseholdAccountBook (スマート家計簿)

## 概要
Spring Boot + MySQL + Dockerを使用した家計簿アプリケーションです。

## 技術スタック
- **Backend**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **Container**: Docker & Docker Compose
- **Frontend**: モダンなWeb UI
- **Security**: AWS Cognito統合
- **OpenAPI**: Swagger

## 機能
- 家計の収支管理
- セキュアなユーザー認証
- RESTful API

### 3. ローカル環境でのアプリケーションの起動
```bash
# Docker Composeで起動
docker-compose up -d

## プロジェクト構造
```
SmartHouseholdAccountBook/
├── backend/            # Spring Boot アプリケーション
├── frontend/           # フロントエンド
├── docker/             # Docker設定ファイル
├── openapi/            # API仕様書
└── docker-compose.yaml # 開発用
```

## セキュリティ機能
- JWT認証
- AWS Cognito統合
- 環境変数による機密情報管理
- SQLインジェクション対策

## パフォーマンス最適化
- データベース接続プール
- 効率的なクエリ設計
- Dockerコンテナの最適化
