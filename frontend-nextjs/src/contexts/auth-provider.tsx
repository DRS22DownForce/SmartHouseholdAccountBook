"use client"

import { Amplify } from "aws-amplify"
import { Authenticator, translations } from "@aws-amplify/ui-react"
import "@aws-amplify/ui-react/styles.css"
import awsConfig from "@/config/aws-exports"
import { I18n } from "aws-amplify/utils"

Amplify.configure(awsConfig)

I18n.putVocabularies(translations)
I18n.setLanguage("ja")

const japaneseLabels = {
  "Sign In": "ログイン",
  "Sign Up": "新規登録",
  "Sign Out": "ログアウト",
  "Sign in": "ログイン",
  "Sign up": "新規登録",
  "Enter your Email": "メールアドレスを入力",
  "Enter your Password": "パスワードを入力",
  "Please confirm your Password": "パスワードを確認",
  Email: "メールアドレス",
  Password: "パスワード",
  "Confirm Password": "パスワード（確認）",
  "Forgot your password?": "パスワードをお忘れですか？",
  "Reset Password": "パスワードリセット",
  "No account?": "アカウントをお持ちでないですか？",
  "Create Account": "アカウントを作成",
  "Have an account?": "アカウントをお持ちですか？",
  "Back to Sign In": "ログイン画面に戻る",
  "Send Code": "コードを送信",
  Code: "確認コード",
  Confirm: "確認",
  "Resend Code": "コードを再送信",
  Submit: "送信",
  "Confirm Sign Up": "サインアップの確認",
  "Confirmation Code": "確認コード",
  "Enter your code": "確認コードを入力",
  "Lost your code?": "コードを紛失しましたか？",
  Skip: "スキップ",
} as const

I18n.putVocabularies({
  ja: japaneseLabels,
})

interface AuthProviderProps {
  children: React.ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  return <Authenticator>{children}</Authenticator>
}

