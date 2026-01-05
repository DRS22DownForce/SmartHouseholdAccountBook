"use client"

import type React from "react"

import { useState, useRef, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { ScrollArea } from "@/components/ui/scroll-area"
import { sendChatMessage, getChatHistory } from "@/api/chatApi"
import { MessageCircle, Send, Loader2 } from "lucide-react"

interface Message {
  id: string
  role: "USER" | "ASSISTANT"
  content: string
}

export function AiChatDialog() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)

  // ダイアログが開いた時に会話履歴を取得
  useEffect(() => {
    if (open && messages.length === 0) {
      loadChatHistory()
    }
  }, [open])

  // メッセージが更新されたらスクロール
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollIntoView({ behavior: "smooth" })
    }
  }, [messages, isLoading])

  /**
   * 会話履歴を取得して表示する
   */
  const loadChatHistory = async () => {
    setIsLoadingHistory(true)
    try {
      const history = await getChatHistory()
      // バックエンドから取得した履歴をMessage形式に変換
      const historyMessages: Message[] = history.map((msg) => ({
        id: msg.id?.toString() || crypto.randomUUID(),
        role: msg.role === "USER" ? "USER" : "ASSISTANT",
        content: msg.content || "",
      }))
      setMessages(historyMessages)
    } catch (error) {
      console.error("Failed to load chat history:", error)
      // エラーが発生しても空の配列を設定（新規会話として開始）
      setMessages([])
    } finally {
      setIsLoadingHistory(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!input.trim() || isLoading) return

    const userMessage: Message = {
      id: crypto.randomUUID(),
      role: "USER",
      content: input.trim(),
    }

    setMessages((prev) => [...prev, userMessage])
    setInput("")
    setIsLoading(true)

    try {
      const data = await sendChatMessage(userMessage.content)
      const assistantMessage: Message = {
        id: crypto.randomUUID(),
        role: "ASSISTANT",
        content: data.message,
      }
      setMessages((prev) => [...prev, assistantMessage])
    } catch (error) {
      console.error("Chat error:", error)
      const errorMessage: Message = {
        id: crypto.randomUUID(),
        role: "ASSISTANT",
        content: "申し訳ございません。エラーが発生しました。もう一度お試しください。",
      }
      setMessages((prev) => [...prev, errorMessage])
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" className="w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 text-muted-foreground hover:text-foreground">
          <MessageCircle className="h-5 w-5" />
          <span className="text-sm font-medium">AIチャット</span>
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl h-[600px] flex flex-col">
        <DialogHeader>
          <DialogTitle>AI家計アドバイザー</DialogTitle>
        </DialogHeader>

        <ScrollArea className="flex-1 pr-4 min-h-0">
          <div className="space-y-4">
            {isLoadingHistory && (
              <div className="text-center py-8 text-muted-foreground">
                <Loader2 className="h-8 w-8 mx-auto mb-4 animate-spin opacity-50" />
                <p className="text-sm">会話履歴を読み込んでいます...</p>
              </div>
            )}
            {!isLoadingHistory && messages.length === 0 && (
              <div className="text-center py-8 text-muted-foreground">
                <MessageCircle className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p className="text-sm">支出の改善方法について相談してみましょう</p>
                <p className="text-xs mt-2">例: 「今月の支出を分析してください」</p>
              </div>
            )}

            {messages.map((message) => (
              <div key={message.id} className={`flex ${message.role === "USER" ? "justify-end" : "justify-start"}`}>
                <div
                  className={`max-w-[80%] rounded-lg px-4 py-2 ${message.role === "USER" ? "bg-primary text-primary-foreground" : "bg-muted text-foreground"
                    }`}
                >
                  <p className="text-sm whitespace-pre-wrap">{message.content}</p>
                </div>
              </div>
            ))}

            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-muted rounded-lg px-4 py-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                </div>
              </div>
            )}
            <div ref={scrollRef} />
          </div>
        </ScrollArea>

        <form onSubmit={handleSubmit} className="flex gap-2 pt-4 border-t">
          <Input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="メッセージを入力..."
            disabled={isLoading}
            className="flex-1"
          />
          <Button type="submit" disabled={isLoading || !input.trim()}>
            <Send className="h-4 w-4" />
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  )
}
