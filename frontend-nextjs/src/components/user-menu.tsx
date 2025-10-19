/**
 * ユーザーメニューコンポーネント
 * 
 * ヘッダー右上に表示されるユーザーメニューです。
 * 
 * 機能:
 * - ログイン中のユーザー名（メールアドレス）を表示
 * - ログアウトボタンを提供
 * 
 * Cognito連携:
 * - onLogout関数でCognitoからログアウト
 * - ログアウト後はAuthProviderが自動的にログイン画面を表示
 */

"use client"

import { User, LogOut } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"

interface UserMenuProps {
  username: string
  onLogout: () => void
}

export function UserMenu({ username, onLogout }: UserMenuProps) {
  if (!username) {
    return null
  }

  /**
   * ログアウトハンドラー
   * 
   * Cognitoからログアウトを実行します。
   * ログアウト後、AuthProviderが認証状態の変更を検知して
   * 自動的にログイン画面を表示するため、明示的なリダイレクトは不要です。
   */
  const handleLogout = () => {
    onLogout()
    // AuthProviderが自動的にログイン画面に戻すため、
    // router.push("/login")は不要
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="icon" className="h-10 w-10 rounded-full border-2 bg-transparent">
          <User className="h-5 w-5" />
          <span className="sr-only">ユーザーメニュー</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel>
          <div className="flex flex-col space-y-1">
            <p className="text-sm font-medium leading-none">ユーザー情報</p>
            <p className="text-xs leading-none text-muted-foreground mt-1">{username}</p>
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={handleLogout} className="text-destructive focus:text-destructive cursor-pointer">
          <LogOut className="mr-2 h-4 w-4" />
          <span>ログアウト</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
