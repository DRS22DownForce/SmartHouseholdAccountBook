import type { LucideIcon } from "lucide-react"
import { Sparkles } from "lucide-react"

interface PageHeaderProps {
  icon: LucideIcon
  title: string
  description: string
}

/** ページ上部のアイコン＋タイトル＋説明文 */
export function PageHeader({ icon: Icon, title, description }: PageHeaderProps) {
  return (
    <div className="animate-fade-in">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="absolute inset-0 bg-orange-500/20 rounded-xl blur-md" />
            <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-orange-400 to-rose-500 text-white shadow-lg">
              <Icon className="h-5 w-5" />
            </div>
          </div>
          <div>
            <h1 className="text-2xl md:text-3xl font-black text-foreground tracking-tight">
              {title}
            </h1>
            <p className="text-sm text-muted-foreground flex items-center gap-1">
              <Sparkles className="w-3 h-3" />
              {description}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
