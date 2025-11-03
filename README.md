# 💰 Smart Household Account Book (スマート家計簿)

> モダンなWeb技術を活用した、直感的で使いやすい家計簿管理アプリケーション。支出の記録・分析・可視化を一つのプラットフォームで実現します。

## アプリケーション画面

![ダッシュボード画面](docs/images/dashboard.png)

**ダッシュボードの主な機能:**
- 📊 過去3ヶ月間の支出推移を棒グラフで表示
- 📋 月別の支出明細リスト（編集・削除機能付き）
- 📈 カテゴリー別支出の割合をドーナツグラフで可視化
- 📥 CSVインポート機能

## 🎯 プロジェクト概要

スマート家計簿は、個人の支出を効率的に管理・分析するためのフルスタックWebアプリケーションです。最新の技術スタックを採用し、RESTful API設計、セキュアな認証、そして直感的なUIを実現しています。

### 主な特徴

- ✨ **モダンなUI/UX**: Next.js 15とTailwind CSSを使用したレスポンシブデザイン
- 🔐 **セキュアな認証**: AWS Cognitoによる認証・認可
- 📊 **データ可視化**: Rechartsによるインタラクティブなグラフ表示
- 🔄 **API設計**: OpenAPI/Swaggerによる型安全なAPI開発
- 🐳 **コンテナ化**: Docker Composeによる簡単な環境構築

## 🛠️ 技術スタック

### フロントエンド
- **フレームワーク**: Next.js 15.5.4 (App Router)
- **UIライブラリ**: React 19.1.0
- **言語**: TypeScript 5.0
- **スタイリング**: Tailwind CSS 4.1
- **UIコンポーネント**: 
  - Radix UI (アクセシブルなコンポーネント)
  - Lucide React (アイコン)
  - Recharts 2.15 (グラフ可視化)
- **フォーム管理**: React Hook Form + Zod (バリデーション)
- **状態管理**: React Hooks
- **認証**: AWS Amplify
- **APIクライアント**: Axios (OpenAPI Generatorで自動生成)

### バックエンド
- **フレームワーク**: Spring Boot 3.x
- **言語**: Java
- **データベース**: MySQL 8.0
- **認証**: AWS Cognito
- **API仕様**: OpenAPI 3.0 (Swagger)
- **依存性注入**: Spring Framework

### インフラストラクチャ・ツール
- **コンテナ化**: Docker & Docker Compose
- **API設計**: OpenAPI Generator (コード自動生成)
- **バージョン管理**: Git

## 🚀 主な機能

### 1. 支出管理
- ✅ 支出の追加・編集・削除
- ✅ カテゴリー別の分類（食費、光熱費、住居費、交通費、その他）
- ✅ 日付による検索・フィルタリング
- ✅ 月別の支出一覧表示

### 2. データ可視化
- 📊 **支出の推移**: 過去数ヶ月間の支出を棒グラフで表示
- 🍩 **カテゴリー別割合**: ドーナツグラフによる支出割合の可視化
- 📈 **月別サマリー**: 合計支出、件数、トップカテゴリーの表示

### 3. データインポート/エクスポート
- 📥 CSVファイルからの支出データインポート（予定）
- 📤 データエクスポート機能（予定）

### 4. セキュリティ機能
- 🔐 AWS Cognitoによるユーザー認証・認可
- 🛡️ JWT トークンによるAPI認証
- 🔒 SQLインジェクション対策
- 🔐 環境変数による機密情報管理

## 📁 プロジェクト構造

```
SmartHouseholdAccountBook/
├── backend/                    # Spring Boot アプリケーション
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/backend/
│   │   │   │       ├── controller/      # REST API コントローラー
│   │   │   │       ├── service/         # ビジネスロジック
│   │   │   │       ├── entity/          # エンティティクラス
│   │   │   │       ├── config/          # 設定クラス（セキュリティ等）
│   │   │   │       └── generated/       # OpenAPI生成コード
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── pom.xml                       # Maven依存関係
│   └── Dockerfile
│
├── frontend-nextjs/           # Next.js アプリケーション
│   ├── src/
│   │   ├── app/                # App Router (Next.js 15)
│   │   │   ├── page.tsx       # ダッシュボードページ
│   │   │   └── api/           # API Routes
│   │   ├── components/        # React コンポーネント
│   │   └── api/               # API クライアント（自動生成）
│   ├── public/                # 静的ファイル
│   ├── package.json
│   └── tailwind.config.js
│
├── docker/                     # Docker設定ファイル
├── openapi/                    # API仕様書
│   └── openapi.yaml           # OpenAPI 3.0仕様
├── docs/                       # ドキュメント
│   └── images/                # スクリーンショット画像
├── docker-compose.yaml         # 開発環境構築用
└── README.md
```

## 🏃‍♂️ セットアップと実行方法

### 前提条件
- Docker & Docker Compose がインストールされていること
- Node.js 18以上（フロントエンド開発用）
- Java 17以上 & Maven（バックエンド開発用）

### 1. リポジトリのクローン
```bash
git clone https://github.com/your-username/SmartHouseholdAccountBook.git
cd SmartHouseholdAccountBook
```

### 2. 環境変数の設定

#### フロントエンド
`frontend-nextjs/.env.local` を作成:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_AWS_REGION=your-aws-region
NEXT_PUBLIC_USER_POOL_ID=your-user-pool-id
NEXT_PUBLIC_CLIENT_ID=your-client-id
OPENAI_API_KEY=your-openai-api-key
```

#### バックエンド
`backend/src/main/resources/application.properties` を編集:
```properties
spring.datasource.url=jdbc:mysql://mysql:3306/accountbook
spring.datasource.username=root
spring.datasource.password=password
aws.cognito.region=your-aws-region
aws.cognito.userPoolId=your-user-pool-id
```

### 3. Docker Composeで起動
```bash
# すべてのサービスを起動（バックエンド、フロントエンド、MySQL）
docker-compose up -d

# ログの確認
docker-compose logs -f

# サービスの停止
docker-compose down
```

### 4. 個別開発モード（推奨）

#### バックエンド
```bash
cd backend
mvn spring-boot:run
```
バックエンドAPI: `http://localhost:8080`

#### フロントエンド
```bash
cd frontend-nextjs
npm install
npm run dev
```
フロントエンド: `http://localhost:3000`

### 5. API仕様書の確認
Swagger UI: `http://localhost:8080/swagger-ui.html`（開発環境）

## 🔒 セキュリティ実装

### 認証・認可
- **AWS Cognito**: マネージド認証サービスによるセキュアなユーザー管理
- **JWT認証**: トークンベースの認証によるステートレスなAPI設計
- **フィルター**: Spring Security によるリクエストフィルタリング

### データ保護
- **SQLインジェクション対策**: PreparedStatementによるパラメータ化クエリ
- **環境変数管理**: 機密情報のハードコーディングを防止
- **HTTPS**: 本番環境での通信暗号化（推奨）

## 📊 API設計

このプロジェクトでは、OpenAPI 3.0仕様を使用してAPIを設計しています。

### 主要エンドポイント

- `GET /api/expenses` - 支出一覧取得
- `POST /api/expenses` - 支出追加
- `PUT /api/expenses/{id}` - 支出更新
- `DELETE /api/expenses/{id}` - 支出削除

詳細は `openapi/openapi.yaml` を参照してください。

## 🚧 今後の拡張予定

- [ ] AIチャット機能の追加
- [ ] 収入管理機能の追加
- [ ] 予算設定・管理機能
- [ ] レポート生成機能（PDF出力）
- [ ] 複数通貨対応
- [ ] モバイルアプリ（React Native）
- [ ] 家計簿テンプレート機能
- [ ] 共同家計簿機能（複数ユーザー）
- [ ] 自動分類機能（AI）


