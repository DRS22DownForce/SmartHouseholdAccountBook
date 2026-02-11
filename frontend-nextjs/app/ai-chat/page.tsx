"use client"

/**
 * AIチャットページコンポーネント
 * 
 * AIアシスタントと対話して節約のアドバイスや分析を受けることができます。
 * グラスモーフィズム効果とアニメーションでリッチなUI/UXを提供します。
 * 
 * 【初心者向け解説】
 * - このページはAIチャットボットとの対話画面です
 * - 家計に関する質問をすると、AIが分析やアドバイスをしてくれます
 * - グラデーションとグロー効果で「AI」らしい未来的なデザインを演出
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { getUserDisplayName } from "@/lib/user-utils"
import { ChatContainer } from "@/components/ai-chat/chat-container"
import { BotMessageSquare, Sparkles, Cpu, Zap } from "lucide-react"
import { cn } from "@/lib/utils"

export default function AiChatPage() {
    const { user, signOut } = useAuthenticator((context) => [context.user])
    const username = useMemo(() => getUserDisplayName(user), [user])

    return (
        <AppLayout username={username} onLogout={signOut}>
            <div className="flex flex-col h-[calc(100vh-8rem)] md:h-[calc(100vh-10rem)] space-y-4 md:space-y-6">
                {/* ==================== */}
                {/* ヘッダーセクション */}
                {/* ==================== */}
                <div className="animate-fade-in">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            {/* グローイングAIアイコン - 特別感を演出 */}
                            <div className="relative">
                                {/* グロー効果（ぼかし） */}
                                <div className="absolute inset-0 bg-gradient-to-br from-purple-500/30 to-pink-500/30 rounded-xl blur-lg animate-pulse" />
                                {/* メインアイコン */}
                                <div className={cn(
                                    "relative flex h-10 w-10 items-center justify-center rounded-xl",
                                    "bg-gradient-to-br from-purple-500 via-violet-500 to-pink-500",
                                    "text-white shadow-lg",
                                    "animate-pulse-glow"
                                )}>
                                    <BotMessageSquare className="h-5 w-5" />
                                </div>
                            </div>
                            <div>
                                <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight flex items-center gap-2">
                                    AIチャット
                                    {/* AIバッジ */}
                                    <span className={cn(
                                        "inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold",
                                        "bg-gradient-to-r from-purple-500/10 to-pink-500/10",
                                        "text-purple-600 dark:text-purple-400",
                                        "border border-purple-500/20"
                                    )}>
                                        <Sparkles className="w-3 h-3" />
                                        AI Powered
                                    </span>
                                </h1>
                                <p className="text-sm text-muted-foreground flex items-center gap-1">
                                    <Cpu className="w-3 h-3" />
                                    家計の相談や節約のアドバイスを受けられます
                                </p>
                            </div>
                        </div>

                        {/* ステータスインジケーター */}
                        <div className={cn(
                            "hidden md:flex items-center gap-2 px-3 py-1.5 rounded-full",
                            "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400",
                            "border border-emerald-500/20"
                        )}>
                            <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                            <span className="text-xs font-bold">オンライン</span>
                        </div>
                    </div>

                    {/* 機能ヒント */}
                    <div className="mt-4 flex flex-wrap gap-2">
                        {[
                            { icon: Zap, text: "節約アドバイス" },
                            { icon: Sparkles, text: "支出分析" },
                            { icon: BotMessageSquare, text: "何でも質問OK" },
                        ].map((hint, index) => (
                            <div
                                key={hint.text}
                                className={cn(
                                    "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg",
                                    "bg-muted/50 text-muted-foreground",
                                    "text-xs font-medium",
                                    "transition-colors hover:bg-muted hover:text-foreground",
                                    "animate-fade-in",
                                    index === 0 ? "stagger-1" : index === 1 ? "stagger-2" : "stagger-3"
                                )}
                            >
                                <hint.icon className="w-3.5 h-3.5" />
                                {hint.text}
                            </div>
                        ))}
                    </div>
                </div>

                {/* ==================== */}
                {/* チャットコンテナ */}
                {/* ==================== */}
                <div className={cn(
                    "flex-1 min-h-0 relative",
                    "animate-fade-in stagger-2"
                )}>
                    {/* 背景装飾 */}
                    <div className="absolute inset-0 rounded-2xl overflow-hidden pointer-events-none">
                        <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-bl from-purple-500/5 to-transparent" />
                        <div className="absolute bottom-0 left-0 w-64 h-64 bg-gradient-to-tr from-pink-500/5 to-transparent" />
                    </div>
                    
                    <ChatContainer />
                </div>
            </div>
        </AppLayout>
    )
}
