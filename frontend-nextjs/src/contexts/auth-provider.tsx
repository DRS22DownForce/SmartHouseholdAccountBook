"use client"

import { Amplify } from "aws-amplify"
import { Authenticator, translations } from "@aws-amplify/ui-react"
import "@aws-amplify/ui-react/styles.css"
import awsConfig from "@/config/aws-exports"
import { I18n } from "aws-amplify/utils"

Amplify.configure(awsConfig)

I18n.putVocabularies(translations) //Amplifyの翻訳データを読み込み
I18n.setLanguage("ja")

export function AuthProvider({ children }: { children: React.ReactNode }) {
  return <Authenticator>{children}</Authenticator>
}

