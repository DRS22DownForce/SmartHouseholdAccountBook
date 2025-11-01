import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/auth-provider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "スマート家計簿アプリ",
  description: "AWS Cognitoで保護された家計簿管理アプリケーション",
};

/**
 * RootLayout
 * 
 * アプリケーション全体の基本レイアウトを定義します。
 * AuthProviderでラップすることで、全ページでCognito認証が必須になります。
 * 
 * 言語設定:
 * - htmlのlang属性を"ja"に設定して日本語サイトとして認識させる
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ja">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {/* 認証プロバイダーで全体をラップ */}
        {/* これにより、未認証ユーザーは自動的にログイン画面に誘導されます */}
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
