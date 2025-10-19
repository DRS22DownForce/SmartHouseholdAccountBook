/**
 * 認証プロバイダー
 * 
 * このコンポーネントはアプリケーション全体にAWS Cognito認証機能を提供します。
 * 
 * 主な役割:
 * 1. AWS Amplifyの初期化 - Cognito接続設定を適用
 * 2. 認証状態の管理 - ログイン/ログアウト状態を監視
 * 3. 認証UIの提供 - Amplify UIコンポーネントで自動的にログイン画面を表示
 * 
 * 技術的なポイント:
 * - "use client"ディレクティブ: Next.js App Routerでクライアントサイドコンポーネントとして動作
 * - Authenticator: 未認証時に自動でログイン/サインアップ画面を表示
 * - 日本語対応: I18nを使用して全てのUIテキストを日本語化
 */

"use client"

import { Amplify } from 'aws-amplify'
import { Authenticator, translations } from '@aws-amplify/ui-react'
import '@aws-amplify/ui-react/styles.css'
import awsConfig from '@/config/aws-exports'
import { I18n } from 'aws-amplify/utils'

// AWS Amplifyの初期化
// この設定により、AmplifyがCognito User Poolに接続できるようになります
Amplify.configure(awsConfig)

// UIテキストの日本語化
// Amplify UIコンポーネントの全てのテキストを日本語に翻訳
I18n.putVocabularies(translations)
I18n.setLanguage('ja')

// 日本語のカスタムラベル
// デフォルトの翻訳をさらにわかりやすい日本語に調整
I18n.putVocabularies({
    ja: {
        'Sign In': 'ログイン',
        'Sign Up': '新規登録',
        'Sign Out': 'ログアウト',
        'Sign in': 'ログイン',
        'Sign up': '新規登録',
        'Enter your Email': 'メールアドレスを入力',
        'Enter your Password': 'パスワードを入力',
        'Please confirm your Password': 'パスワードを確認',
        'Email': 'メールアドレス',
        'Password': 'パスワード',
        'Confirm Password': 'パスワード（確認）',
        'Forgot your password?': 'パスワードをお忘れですか？',
        'Reset Password': 'パスワードリセット',
        'No account?': 'アカウントをお持ちでないですか？',
        'Create Account': 'アカウントを作成',
        'Have an account?': 'アカウントをお持ちですか？',
        'Back to Sign In': 'ログイン画面に戻る',
        'Send Code': 'コードを送信',
        'Code': '確認コード',
        'Confirm': '確認',
        'Resend Code': 'コードを再送信',
        'Submit': '送信',
        'Confirm Sign Up': 'サインアップの確認',
        'Confirmation Code': '確認コード',
        'Enter your code': '確認コードを入力',
        'Lost your code?': 'コードを紛失しましたか？',
        'Skip': 'スキップ',
    }
})

type AuthProviderProps = {
    children: React.ReactNode
}

/**
 * AuthProviderコンポーネント
 * 
 * @param children - 認証が必要な子コンポーネント
 * 
 * 動作:
 * - ユーザーが未認証の場合: ログイン画面を自動表示
 * - ユーザーが認証済みの場合: 子コンポーネント（アプリのメイン画面）を表示
 * 
 * セキュリティ:
 * - 認証トークンはAmplifyが自動管理（安全なストレージに保存）
 * - セッションの有効期限が切れると自動的にログイン画面に戻る
 */
export function AuthProvider({ children }: AuthProviderProps) {
    return (
        <Authenticator>
            {children}
        </Authenticator>
    )
}

