"use client"

import type React from "react"
import { useState, useRef, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { ScrollArea } from "@/components/ui/scroll-area"
import { sendChatMessage, getChatHistory } from "@/api/chatApi"
import { BotMessageSquare, Send, Loader2, Sparkles } from "lucide-react"

interface Message {
    id: string
    role: "USER" | "ASSISTANT"
    content: string
}

export function ChatContainer() {
    const [messages, setMessages] = useState<Message[]>([])
    const [input, setInput] = useState("")
    const [isLoading, setIsLoading] = useState(false)
    const [isLoadingHistory, setIsLoadingHistory] = useState(false)
    const scrollRef = useRef<HTMLDivElement>(null)

    // 初期化時に会話履歴を取得
    useEffect(() => {
        loadChatHistory()
    }, [])

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
        <div className="flex flex-col h-full w-full bg-background rounded-xl overflow-hidden border border-border/50 shadow-sm relative">
            {/* チャットエリア - フレックスボックスで残りの高さを占有 */}
            <div className="flex-1 min-h-0 relative">
                <ScrollArea className="h-full w-full p-4 md:p-6">
                    <div className="space-y-6 pb-4">
                        {isLoadingHistory && (
                            <div className="flex flex-col items-center justify-center py-12 text-muted-foreground animate-pulse">
                                <Loader2 className="h-8 w-8 mb-2 animate-spin text-purple-500" />
                                <p className="text-sm font-medium">履歴を読み込んでいます...</p>
                            </div>
                        )}

                        {!isLoadingHistory && messages.length === 0 && (
                            <div className="flex flex-col items-center justify-center py-20 text-center animate-in fade-in zoom-in duration-500 max-w-lg mx-auto">
                                <div className="h-24 w-24 rounded-2xl bg-gradient-to-br from-purple-500/10 to-blue-500/10 flex items-center justify-center mb-8 ring-1 ring-purple-500/20 shadow-lg shadow-purple-500/5">
                                    <BotMessageSquare className="h-12 w-12 text-purple-600" />
                                </div>
                                <h3 className="text-xl md:text-2xl font-bold bg-gradient-to-r from-foreground to-muted-foreground bg-clip-text text-transparent mb-3">
                                    AI家計アドバイザー
                                </h3>
                                <p className="text-muted-foreground mb-8 leading-relaxed">
                                    家計の分析、節約のアドバイス、将来の予測など。<br />
                                    あなたの専属アドバイザーがお手伝いします。
                                </p>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full">
                                    {[
                                        "今月の支出を分析して",
                                        "先月との比較を見せて",
                                        "食費を抑えるコツは？",
                                        "予算の達成状況を教えて"
                                    ].map((text) => (
                                        <Button
                                            key={text}
                                            variant="outline"
                                            className="h-auto py-3 px-4 justify-start text-left font-normal hover:bg-muted/50 hover:text-purple-600 transition-all border-border/60"
                                            onClick={() => setInput(text)}
                                        >
                                            <Sparkles className="h-4 w-4 mr-2 text-yellow-500 shrink-0" />
                                            <span className="truncate">{text}</span>
                                        </Button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {messages.map((message) => (
                            <div
                                key={message.id}
                                className={`flex ${message.role === "USER" ? "justify-end" : "justify-start"} animate-in slide-in-from-bottom-2 duration-300`}
                            >
                                {message.role === "ASSISTANT" && (
                                    <div className="flex-shrink-0 mr-3 mt-1">
                                        <div className="h-8 w-8 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-600">
                                            <BotMessageSquare className="h-5 w-5" />
                                        </div>
                                    </div>
                                )}
                                <div
                                    className={`max-w-[85%] md:max-w-[75%] rounded-2xl px-5 py-3.5 shadow-sm text-sm leading-relaxed whitespace-pre-wrap ${message.role === "USER"
                                        ? "bg-primary text-primary-foreground rounded-tr-sm"
                                        : "bg-muted/80 text-foreground rounded-tl-sm border border-border/50"
                                        }`}
                                >
                                    {message.content}
                                </div>
                            </div>
                        ))}

                        {isLoading && (
                            <div className="flex justify-start animate-in fade-in duration-300">
                                <div className="flex-shrink-0 mr-3 mt-1">
                                    <div className="h-8 w-8 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-600">
                                        <BotMessageSquare className="h-5 w-5" />
                                    </div>
                                </div>
                                <div className="bg-muted/80 text-foreground rounded-2xl rounded-tl-sm border border-border/50 px-5 py-4 shadow-sm">
                                    <div className="flex items-center gap-1.5">
                                        <span className="w-1.5 h-1.5 bg-foreground/30 rounded-full animate-bounce"></span>
                                        <span className="w-1.5 h-1.5 bg-foreground/30 rounded-full animate-bounce [animation-delay:0.2s]"></span>
                                        <span className="w-1.5 h-1.5 bg-foreground/30 rounded-full animate-bounce [animation-delay:0.4s]"></span>
                                    </div>
                                </div>
                            </div>
                        )}
                        <div ref={scrollRef} className="h-1" />
                    </div>
                </ScrollArea>
            </div>

            {/* 入力エリア - 下部に固定 */}
            <div className="p-4 md:p-5 border-t border-border/50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <form onSubmit={handleSubmit} className="flex gap-3 max-w-4xl mx-auto relative">
                    <Input
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        placeholder="メッセージを入力..."
                        disabled={isLoading}
                        className="flex-1 bg-muted/50 border-transparent focus:border-primary/20 focus:bg-background focus:ring-2 focus:ring-primary/10 transition-all text-base py-6 pl-5 pr-12 rounded-xl"
                    />
                    <Button
                        type="submit"
                        disabled={isLoading || !input.trim()}
                        size="icon"
                        className="absolute right-2 top-1.5 h-9 w-9 rounded-lg bg-primary hover:bg-primary/90 transition-all hover:scale-105 shadow-sm"
                    >
                        <Send className="h-4 w-4 fill-current" />
                        <span className="sr-only">送信</span>
                    </Button>
                </form>
                <div className="text-[10px] text-center mt-3 text-muted-foreground/60 font-medium">
                    AIは正確な情報を生成しない可能性があります。
                </div>
            </div>
        </div>
    )
}
