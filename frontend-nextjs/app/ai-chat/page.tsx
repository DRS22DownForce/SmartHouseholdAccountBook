"use client"

/**
 * AIチャットページコンポーネント
 * 
 * AIアシスタントと対話して節約のアドバイスや分析を受けることができます。
 */

import { useMemo } from "react"
import { useAuthenticator } from "@aws-amplify/ui-react"
import { AppLayout } from "@/components/layout/AppLayout"
import { getUserDisplayName } from "@/lib/user-utils"
import { ChatContainer } from "@/components/ai-chat/chat-container"
import { BotMessageSquare } from "lucide-react"

export default function AiChatPage() {
    const { user, signOut } = useAuthenticator((context) => [context.user])
    const username = useMemo(() => getUserDisplayName(user), [user])

    return (
        <AppLayout username={username} onLogout={signOut}>
            <div className="flex flex-col h-[calc(100vh-8rem)] md:h-[calc(100vh-10rem)] space-y-4 md:space-y-6">
                <div>
                    <h1 className="text-2xl md:text-3xl font-bold text-foreground tracking-tight mb-1 flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-purple-500/10 text-purple-600">
                            <BotMessageSquare className="h-5 w-5" />
                        </div>
                        AIチャット
                    </h1>
                    <p className="text-sm text-muted-foreground">
                        AIアシスタントに家計の相談や節約のアドバイスを求めることができます
                    </p>
                </div>

                <div className="flex-1 min-h-0">
                    <ChatContainer />
                </div>
            </div>
        </AppLayout>
    )
}
