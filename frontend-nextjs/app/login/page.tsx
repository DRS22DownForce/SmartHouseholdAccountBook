"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Wallet } from "lucide-react"

export default function LoginPage() {
  const [username, setUsername] = useState("")
  const router = useRouter()

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (username.trim()) {
      localStorage.setItem("household-user", username.trim())
      router.push("/")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950 px-4">
      <Card className="w-full max-w-md shadow-2xl border-border/50">
        <CardHeader className="space-y-6 text-center pb-8">
          <div className="flex justify-center">
            <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-primary/80 shadow-xl shadow-primary/25">
              <Wallet className="h-10 w-10 text-primary-foreground" />
            </div>
          </div>
          <div>
            <CardTitle className="text-4xl font-bold tracking-tight">家計簿アプリ</CardTitle>
            <CardDescription className="text-base mt-3">ユーザー名を入力してログインしてください</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="pb-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="username" className="text-sm font-semibold">
                ユーザー名
              </Label>
              <Input
                id="username"
                type="text"
                placeholder="山田太郎"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                className="text-base h-12 rounded-lg"
                autoFocus
              />
            </div>
            <Button
              type="submit"
              className="w-full h-12 text-base font-semibold rounded-lg shadow-lg shadow-primary/25"
              size="lg"
            >
              ログイン
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
