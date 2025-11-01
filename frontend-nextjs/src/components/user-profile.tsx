"use client"

import { useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { User } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

interface UserProfileProps {
  username: string
  onSave: (name: string) => void
}

export function UserProfile({ username, onSave }: UserProfileProps) {
  const [tempName, setTempName] = useState(username)

  if (!username) {
    return (
      null
    )
  }

  return (
    <Card className="bg-card/50">
      <CardContent className="pt-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
            <User className="h-5 w-5 text-primary" />
          </div>
          <div>
            <p className="text-sm text-muted-foreground">ユーザー</p>
            <p className="font-semibold text-foreground">{username}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
