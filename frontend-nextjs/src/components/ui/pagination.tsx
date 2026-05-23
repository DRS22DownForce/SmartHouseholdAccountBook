"use client"

/**
 * Paginationコンポーネント
 *
 * データ一覧のページ切替 UI。
 * scrollRestoreReady を渡すと、ページ切替後もボタン位置が画面内で動かないよう補正する。
 */

import { useCallback, useLayoutEffect, useRef, type ReactNode } from "react"
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  pageSize?: number
  onPageSizeChange?: (size: number) => void
  pageSizeOptions?: number[]
  className?: string
  /** 指定時、ページ切替後にこの UI の画面内位置を維持する（false の間はデータ取得完了を待つ） */
  scrollRestoreReady?: boolean
}

/** クリック時のフォーカス移動によるスクロールジャンプを防ぐページボタン */
function PageButton({
  onClick,
  disabled,
  variant = "outline",
  children,
}: {
  onClick: () => void
  disabled?: boolean
  variant?: "default" | "outline"
  children: ReactNode
}) {
  return (
    <Button
      type="button"
      variant={variant}
      size="sm"
      onMouseDown={(e) => e.preventDefault()}
      onClick={onClick}
      disabled={disabled}
      className="h-9 w-9 p-0"
    >
      {children}
    </Button>
  )
}

export function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  pageSize,
  onPageSizeChange,
  pageSizeOptions = [10, 25, 50, 100],
  className,
  scrollRestoreReady,
}: PaginationProps) {
  const rootRef = useRef<HTMLDivElement>(null)
  const anchorTopRef = useRef<number | null>(null)
  const shouldPreserveScroll = scrollRestoreReady !== undefined

  const changePage = useCallback(
    (page: number) => {
      if (shouldPreserveScroll && rootRef.current) {
        anchorTopRef.current = rootRef.current.getBoundingClientRect().top
      }
      onPageChange(page)
    },
    [onPageChange, shouldPreserveScroll]
  )

  useLayoutEffect(() => {
    if (!shouldPreserveScroll || scrollRestoreReady === false) return
    if (anchorTopRef.current === null || !rootRef.current) return

    const anchorTop = anchorTopRef.current
    anchorTopRef.current = null

    const delta = rootRef.current.getBoundingClientRect().top - anchorTop
    if (Math.abs(delta) > 0.5) {
      window.scrollBy({ top: delta, left: 0 })
    }
  }, [currentPage, scrollRestoreReady, shouldPreserveScroll])

  const getPageNumbers = () => {
    const pages: (number | string)[] = []
    const maxVisible = 5

    if (totalPages <= maxVisible) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i)
      }
    } else {
      let start = Math.max(1, currentPage - Math.floor(maxVisible / 2))
      let end = Math.min(totalPages, start + maxVisible - 1)

      if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1)
      }

      if (start > 1) {
        pages.push(1)
        if (start > 2) {
          pages.push("...")
        }
      }

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }

      if (end < totalPages) {
        if (end < totalPages - 1) {
          pages.push("...")
        }
        pages.push(totalPages)
      }
    }

    return pages
  }

  const pageNumbers = getPageNumbers()

  return (
    <div
      ref={shouldPreserveScroll ? rootRef : undefined}
      className={cn("flex items-center justify-between gap-4", className)}
    >
      {pageSize && onPageSizeChange && (
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">表示件数:</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            className="h-9 rounded-md border border-input bg-background px-3 py-1 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          >
            {pageSizeOptions.map((size) => (
              <option key={size} value={size}>
                {size}件
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="flex items-center gap-1">
        <PageButton onClick={() => changePage(1)} disabled={currentPage === 1}>
          <ChevronsLeft className="h-4 w-4" />
          <span className="sr-only">最初のページへ</span>
        </PageButton>
        <PageButton
          onClick={() => changePage(currentPage - 1)}
          disabled={currentPage === 1}
        >
          <ChevronLeft className="h-4 w-4" />
          <span className="sr-only">前のページへ</span>
        </PageButton>

        {pageNumbers.map((page, index) => {
          if (page === "...") {
            return (
              <span key={`ellipsis-${index}`} className="px-2 text-sm text-muted-foreground">
                ...
              </span>
            )
          }

          const pageNum = page as number
          return (
            <PageButton
              key={pageNum}
              variant={currentPage === pageNum ? "default" : "outline"}
              onClick={() => changePage(pageNum)}
            >
              {pageNum}
            </PageButton>
          )
        })}

        <PageButton
          onClick={() => changePage(currentPage + 1)}
          disabled={currentPage === totalPages}
        >
          <ChevronRight className="h-4 w-4" />
          <span className="sr-only">次のページへ</span>
        </PageButton>
        <PageButton
          onClick={() => changePage(totalPages)}
          disabled={currentPage === totalPages}
        >
          <ChevronsRight className="h-4 w-4" />
          <span className="sr-only">最後のページへ</span>
        </PageButton>
      </div>
    </div>
  )
}
