/**
 * AWS Amplify設定
 * 
 * このファイルはAWS Cognitoの接続設定を定義しています。
 * - aws_project_region: AWSプロジェクトのリージョン
 * - aws_cognito_identity_pool_id: Cognito Identity Poolの識別子
 * - aws_cognito_region: Cognitoが配置されているリージョン
 * - aws_user_pools_id: Cognito User Poolの識別子
 * - aws_user_pools_web_client_id: WebアプリケーションのクライアントID
 * 
 * 環境変数の設定方法:
 * frontend-nextjs/.env.local に以下の環境変数を設定してください:
 * - NEXT_PUBLIC_AWS_REGION: AWSリージョン（例: ap-northeast-1）
 * - NEXT_PUBLIC_COGNITO_IDENTITY_POOL_ID: Cognito Identity Pool ID
 * - NEXT_PUBLIC_COGNITO_USER_POOL_ID: Cognito User Pool ID
 * - NEXT_PUBLIC_COGNITO_CLIENT_ID: Cognito Client ID
 * 
 * セキュリティ注意:
 * - これらの値は公開されても問題ありませんが、本番環境では環境変数から読み込むことを推奨
 * - クライアントIDは公開情報で、実際の認証はCognitoサーバー側で検証されます
 * - 環境変数が設定されていない場合は、プレースホルダー値が使用されます（本番環境では必ず環境変数を設定してください）
 */

const awsConfig = {
    // AWSプロジェクトの基本設定
    aws_project_region: process.env.NEXT_PUBLIC_AWS_REGION || "ap-northeast-1",

    // Cognito Identity Pool（フェデレーテッドIDを管理）
    aws_cognito_identity_pool_id: process.env.NEXT_PUBLIC_COGNITO_IDENTITY_POOL_ID || "your-identity-pool-id",

    // Cognito User Poolの設定（ユーザー認証を管理）
    aws_cognito_region: process.env.NEXT_PUBLIC_AWS_REGION || "ap-northeast-1",
    aws_user_pools_id: process.env.NEXT_PUBLIC_COGNITO_USER_POOL_ID || "your-user-pool-id",
    aws_user_pools_web_client_id: process.env.NEXT_PUBLIC_COGNITO_CLIENT_ID || "your-client-id",

    // OAuth設定（現在は未使用）
    oauth: {},

    // サインアップ時にメールアドレスをユーザー名として使用
    aws_cognito_username_attributes: [
        "EMAIL"
    ],

    // ソーシャルログインプロバイダー（現在は未設定）
    aws_cognito_social_providers: [],

    // サインアップ時に必要な属性（メールアドレス）
    aws_cognito_signup_attributes: [
        "EMAIL"
    ],

    // 多要素認証（MFA）の設定（現在はOFF）
    aws_cognito_mfa_configuration: "OFF",
    aws_cognito_mfa_types: [
        "SMS"
    ],

    // パスワードポリシーの設定
    aws_cognito_password_protection_settings: {
        passwordPolicyMinLength: 8,  // 最小8文字
        passwordPolicyCharacters: [] // 文字種の制限なし
    },

    // アカウント検証方法（メールで確認コードを送信）
    aws_cognito_verification_mechanisms: [
        "EMAIL"
    ]
};

export default awsConfig;

