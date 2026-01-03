"use client"

/**
 * 共通ローディングスピナーコンポーネント
 * 
 * データの読み込み中に表示するローディング表示を提供します。
 */
export function LoadingSpinner() {
    return (
        <div className="min-h-screen flex items-center justify-center">
            <p className="text-muted-foreground">読み込み中...</p>
        </div>
    )
}

