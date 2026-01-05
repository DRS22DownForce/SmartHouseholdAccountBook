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
import { Upload, FileText, AlertCircle, CheckCircle2 } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import type { ExpenseFormData } from "@/lib/types"
import { parseCSV } from "@/lib/csv-parser"

interface CsvUploadDialogProps {
  onUpload: (expenses: ExpenseFormData[]) => void
}

export function CsvUploadDialog({ onUpload }: CsvUploadDialogProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [status, setStatus] = useState<{ type: "success" | "error"; message: string } | null>(null)
  const [open, setOpen] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFile = async (file: File) => {
    if (!file.name.endsWith(".csv")) {
      setStatus({ type: "error", message: "CSVファイルを選択してください" })
      return
    }

    try {
      const text = await file.text()
      const expenses = parseCSV(text)

      if (expenses.length === 0) {
        setStatus({ type: "error", message: "有効なデータが見つかりませんでした" })
        return
      }

      onUpload(expenses)
      setStatus({ type: "success", message: `${expenses.length}件の支出データをインポートしました` })

      if (fileInputRef.current) {
        fileInputRef.current.value = ""
      }

      setTimeout(() => {
        setOpen(false)
        setStatus(null)
      }, 2000)
    } catch (error) {
      setStatus({
        type: "error",
        message: error instanceof Error ? error.message : "CSVの解析に失敗しました",
      })
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
        <Button variant="ghost" className="w-full justify-start gap-3 px-3 py-2.5 h-auto hover:bg-muted/50 text-muted-foreground hover:text-foreground">
          <Upload className="h-5 w-5" />
          <span className="text-sm font-medium">CSVインポート</span>
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

          {status && (
            <Alert variant={status.type === "error" ? "destructive" : "default"}>
              {status.type === "success" ? <CheckCircle2 className="h-4 w-4" /> : <AlertCircle className="h-4 w-4" />}
              <AlertDescription>{status.message}</AlertDescription>
            </Alert>
          )}

          <div className="text-xs text-muted-foreground space-y-1">
            <p className="font-medium">CSVフォーマット例:</p>
            <code className="block bg-muted p-2 rounded text-xs">
              日付,金額,カテゴリ,説明
              <br />
              2024-01-15,3500,食費,スーパーマーケット
              <br />
              2024-01-16,1200,交通費,電車代
            </code>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
