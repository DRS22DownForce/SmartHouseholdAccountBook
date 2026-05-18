import { redirect } from "next/navigation"

/**
 * ルート（/）へのアクセスを支出ページ（/expenses）へ転送する。
 * Server Component の redirect はサーバー側で即座に遷移するため、
 * クライアントの useEffect よりフラッシュが少なく意図も明確。
 */
export default function RootPage() {
  redirect("/expenses")
}
