"use client"

import type React from "react"
import { useState, useRef } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Upload, FileText, AlertCircle, CheckCircle2, Loader2 } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { uploadCsvFile, type CsvUploadResponse } from "@/api/expenseApi"
import { cn } from "@/lib/utils"

interface CsvUploadDialogProps {
  onUpload?: () => void
}

export function CsvUploadDialog({ onUpload }: CsvUploadDialogProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [status, setStatus] = useState<{ type: "success" | "error"; message: string; details?: CsvUploadResponse } | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [open, setOpen] = useState(false)
  const [csvFormat, setCsvFormat] = useState<"OLD_FORMAT" | "NEW_FORMAT" | "">("")
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFile = async (file: File) => {
    if (!file.name.endsWith(".csv")) {
      setStatus({ type: "error", message: "CSVファイルを選択してください" })
      return
    }

    // CSV形式が選択されていない場合はエラー
    if (!csvFormat || (csvFormat !== "OLD_FORMAT" && csvFormat !== "NEW_FORMAT")) {
      setStatus({ type: "error", message: "CSV形式を選択してください" })
      return
    }

    setIsUploading(true)
    setStatus(null)

    try {
      // バックエンドにCSVファイルを直接アップロード
      const result = await uploadCsvFile(file, csvFormat)

      // 成功メッセージを作成
      let message = `${result.successCount}件の支出データをインポートしました`
      if (result.errorCount > 0) {
        message += `（${result.errorCount}件のエラーがあります）`
      }

      setStatus({
        type: result.errorCount > 0 ? "error" : "success",
        message,
        details: result,
      })

      // コールバックを呼び出し（データを再取得するため）
      if (onUpload) {
        onUpload()
      }

      if (fileInputRef.current) {
        fileInputRef.current.value = ""
      }

      // エラーがない場合は自動的に閉じる
      if (result.errorCount === 0) {
        setTimeout(() => {
          setOpen(false)
          setStatus(null)
          setCsvFormat("") // 形式をリセット
        }, 2000)
      }
    } catch (error) {
      setStatus({
        type: "error",
        message: error instanceof Error ? error.message : "CSVのアップロードに失敗しました",
      })
    } finally {
      setIsUploading(false)
    }
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(true)
  }

  const handleDragLeave = () => {
    setIsDragging(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)

    const file = e.dataTransfer.files[0]
    if (file) {
      handleFile(file)
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      handleFile(file)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" className="w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 text-muted-foreground hover:text-foreground group">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-500/10 text-amber-600 group-hover:bg-amber-500/20 flex-shrink-0 transition-colors">
            <Upload className="h-5 w-5" />
          </div>
          <div className="flex items-center gap-2 flex-1">
            <span className="text-sm font-medium">CSVインポート</span>
            {/* 開発中バッジ */}
            <span className={cn(
              "px-1.5 py-0.5 text-[10px] font-bold rounded-md",
              "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400",
              "border border-amber-300 dark:border-amber-700"
            )}>
              開発中
            </span>
          </div>
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            CSVインポート
          </DialogTitle>
          <DialogDescription>クレジットカードの使用履歴をCSVファイルでアップロード</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          {/* CSV形式選択 */}
          <div className="space-y-2">
            <label className="text-sm font-medium">CSV形式を選択</label>
            <Select value={csvFormat} onValueChange={(value) => setCsvFormat(value as "OLD_FORMAT" | "NEW_FORMAT")}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="CSV形式を選択してください" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="OLD_FORMAT">三井住友カード（2025/12以前）</SelectItem>
                <SelectItem value="NEW_FORMAT">三井住友カード（2026/1以降）</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${isDragging ? "border-primary bg-primary/5" : "border-border hover:border-primary/50"
              }`}
          >
            <Upload className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
            <p className="text-sm text-foreground mb-2">ファイルをドラッグ&ドロップ</p>
            <p className="text-xs text-muted-foreground mb-4">または</p>
            <Button variant="outline" onClick={() => fileInputRef.current?.click()} className="mx-auto">
              ファイルを選択
            </Button>
            <input ref={fileInputRef} type="file" accept=".csv" onChange={handleFileSelect} className="hidden" />
          </div>

          {isUploading && (
            <Alert>
              <Loader2 className="h-4 w-4 animate-spin" />
              <AlertDescription>CSVファイルをアップロード中...</AlertDescription>
            </Alert>
          )}

          {status && (
            <Alert variant={status.type === "error" ? "destructive" : "default"}>
              {status.type === "success" ? <CheckCircle2 className="h-4 w-4" /> : <AlertCircle className="h-4 w-4" />}
              <AlertDescription>
                <div className="space-y-2">
                  <p>{status.message}</p>
                  {status.details && status.details.errors.length > 0 && (
                    <div className="mt-2 text-xs">
                      <p className="font-medium mb-1">エラー詳細:</p>
                      <ul className="list-disc list-inside space-y-1 max-h-32 overflow-y-auto">
                        {status.details.errors.map((error, index) => (
                          <li key={index}>
                            行{error.lineNumber}: {error.message}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              </AlertDescription>
            </Alert>
          )}

          <div className="text-xs text-muted-foreground space-y-1">
            <p className="font-medium">CSVフォーマット:</p>
            <p>三井住友カードの利用明細CSVファイルをアップロードできます。</p>
            <p className="mt-2">対応フォーマット:</p>
            <code className="block bg-muted p-2 rounded text-xs mt-1">
              ご利用日,ご利用店名,ご利用金額,...
              <br />
              2025/11/01,やよい軒平塚店,1220,...
            </code>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
