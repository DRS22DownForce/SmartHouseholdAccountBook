/**
 * 支出データ管理フック
 * 
 * このカスタムフックは、支出データのCRUD操作（作成・読取・更新・削除）を提供します。
 * LocalStorageベースから、バックエンドAPIとの連携に変更されました。
 * 
 * 主な機能:
 * - 支出データの取得（初回ロード時にAPIから取得）
 * - 支出の追加（POST /api/expenses）
 * - 支出の更新（PUT /api/expenses/{id}）
 * - 支出の削除（DELETE /api/expenses/{id}）
 * - エラーハンドリングとトースト通知
 * 
 * セキュリティ:
 * - 全てのAPI呼び出しにJWTトークンを自動付与
 * - Cognitoで認証されたユーザーのみアクセス可能
 * 
 * パフォーマンス:
 * - API成功後のみUI更新: サーバーの応答を確認してから更新
 */

"use client"

import { useState, useEffect } from "react"
import { toast } from "sonner"
import { getApiClient, withAuthHeader } from "@/api/expenseApi"
import type { ExpenseDto, ExpenseRequestDto } from "@/api/generated/api"
import type { Expense, ExpenseFormData } from "@/lib/types"

/**
 * ExpenseDtoをExpense型に変換
 * 
 * APIレスポンスの型をUI用の型に変換します。
 * - id: number → string
 * - createdAt: 現在時刻で設定（APIには存在しないがUI側で必要）
 * 
 * null/undefinedチェック:
 * - OpenAPI仕様でオプショナルなフィールドはデフォルト値を設定
 * - ?? 演算子でnull/undefinedの場合のデフォルト値を指定
 */
function toExpense(dto: ExpenseDto): Expense {
  return {
    id: String(dto.id ?? 0),
    amount: dto.amount ?? 0,  // amountがundefinedの場合は0
    category: dto.category ?? '',  // categoryがundefinedの場合は空文字列
    description: dto.description ?? '',
    date: dto.date ?? '',  // dateがundefinedの場合は空文字列
    createdAt: new Date().toISOString()
  }
}

/**
 * ExpenseFormDataをExpenseRequestDtoに変換
 * 
 * UIのフォームデータをAPI用のリクエスト型に変換します。
 */
function toRequestDto(data: ExpenseFormData): ExpenseRequestDto {
  return {
    date: data.date,
    category: data.category,
    amount: data.amount,
    description: data.description
  }
}

function notifyError(error: any, defaultMessage: string) {

  if (error.response?.status === 401) {
    toast.error('認証エラー: 再ログインしてください')
  } else if (error.response?.status === 404) {
    toast.error('データが見つかりませんでした')
  } else {
    toast.error(defaultMessage)
  }
}

/**
 * 支出データ管理フック
 * 
 * @returns {Object} 支出データと操作関数
 * - expenses: 支出データの配列
 * - addExpense: 支出を追加する関数
 * - addExpenses: 複数の支出を一括追加する関数（CSV インポート用）
 * - updateExpense: 支出を更新する関数
 * - deleteExpense: 支出を削除する関数
 * - isLoaded: データの読み込みが完了したかどうか
 */
export function useExpenses() {
  // 支出データの状態管理
  const [expenses, setExpenses] = useState<Expense[]>([])

  // データ読み込み完了フラグ
  const [isLoaded, setIsLoaded] = useState(false)

  // APIクライアントのインスタンスを作成
  const api = getApiClient()

  /**
   * 初回ロード時に支出データをAPIから取得
   * 
   * useEffectの依存配列が空([])なので、コンポーネントのマウント時に1回だけ実行されます。
   */
  useEffect(() => {
    fetchExpenses()
  }, [])

  /**
   * 支出一覧を取得する関数
   * 
   * GET /api/expenses を呼び出して、全ての支出データを取得します。
   * 
   */
  const fetchExpenses = async () => {
    try {
      // JWT認証ヘッダーを付与
      const options = await withAuthHeader()

      // APIから支出一覧を取得
      const response = await api.apiExpensesGet(options)

      // ExpenseDto[] を Expense[] に変換
      const expenseList = response.data.map(toExpense)

      // 状態を更新
      setExpenses(expenseList)
      setIsLoaded(true)
    } catch (error: any) {
      notifyError(error, '支出データの取得に失敗しました')
      // エラーでも読み込み完了とする（空の配列を表示）
      setIsLoaded(true)
    }
  }

  /**
   * 支出を追加する関数
   * 
   * POST /api/expenses を呼び出して、新しい支出をサーバーに保存します。
   * 
   * @param {ExpenseFormData} data - 追加する支出データ
   */
  const addExpense = async (data: ExpenseFormData) => {
    try {
      // API用のリクエストDTOに変換
      const requestDto = toRequestDto(data)

      // JWT認証ヘッダーを付与
      const options = await withAuthHeader()

      // APIで支出を追加
      const response = await api.apiExpensesPost(requestDto, options)

      // サーバーから返されたデータ（IDを含む）をUI用の型に変換
      const newExpense = toExpense(response.data)

      // 一覧の先頭に追加（最新のものが上に表示される）
      setExpenses((prev) => [newExpense, ...prev])

      // 成功通知
      toast.success('支出を追加しました')
    } catch (error: any) {
      notifyError(error, '支出の追加に失敗しました')
    }
  }

  /**
   * 複数の支出を一括追加する関数（CSV インポート用）
   * 
   * 複数のPOSTリクエストを並列で実行します。
   * 
   * @param {ExpenseFormData[]} dataArray - 追加する支出データの配列
   */
  const addExpenses = async (dataArray: ExpenseFormData[]) => {
    try {
      // JWT認証ヘッダーを付与
      const options = await withAuthHeader()

      // 全ての支出を並列で追加（Promise.all）
      // Promise.all: 全てのPromiseが完了するまで待機
      const responses = await Promise.all(
        dataArray.map(data => api.apiExpensesPost(toRequestDto(data), options))
      )

      // サーバーから返されたデータをUI用の型に変換
      const newExpenses = responses.map(response => toExpense(response.data))

      // 一覧の先頭に追加
      setExpenses((prev) => [...newExpenses, ...prev])

      // 成功通知（件数を表示）
      toast.success(`${newExpenses.length}件の支出を追加しました`)
    } catch (error: any) {
      console.error('Failed to add expenses:', error)

      // エラー通知
      notifyError(error, '支出の一括追加に失敗しました')
    }
  }

  /**
   * 支出を更新する関数
   * 
   * PUT /api/expenses/{id} を呼び出して、既存の支出を更新します。
   * 
   * 更新の流れ:
   * 1. APIを呼び出してサーバーで更新
   * 2. 成功時のみ: サーバーから返されたデータでUIを更新
   * 3. 失敗時: エラー通知のみ（UIは変更されない）
   * 
   * @param {string} id - 更新する支出のID
   * @param {ExpenseFormData} data - 更新後のデータ
   */
  const updateExpense = async (id: string, data: ExpenseFormData) => {
    try {
      // API用のリクエストDTOに変換
      const requestDto = toRequestDto(data)

      // JWT認証ヘッダーを付与
      const options = await withAuthHeader()

      // APIで支出を更新（idをnumberに変換）
      const response = await api.apiExpensesIdPut(Number(id), requestDto, options)

      // サーバーから返されたデータをUI用の型に変換
      const updatedExpense = toExpense(response.data)

      // API成功後にのみUIを更新
      setExpenses((prev) =>
        prev.map((expense) =>
          expense.id === id ? updatedExpense : expense
        )
      )
      // 成功通知
      toast.success('支出を更新しました')
    } catch (error: any) {
      // エラー通知（UIは変更されない）
      notifyError(error, '支出の更新に失敗しました')
    }
  }

  /**
   * 支出を削除する関数
   * 
   * DELETE /api/expenses/{id} を呼び出して、支出をサーバーから削除します。
   * 
   * 削除の流れ:
   * 1. APIを呼び出してサーバーで削除
   * 2. 成功時のみ: UIから削除
   * 3. 失敗時: エラー通知のみ（UIは変更されない）
   * 
   * @param {string} id - 削除する支出のID
   */
  const deleteExpense = async (id: string) => {
    try {
      // JWT認証ヘッダーを付与
      const options = await withAuthHeader()

      // APIで支出を削除（idをnumberに変換）
      await api.apiExpensesIdDelete(Number(id), options)

      // API成功後にのみUIから削除
      setExpenses((prev) => prev.filter((expense) => expense.id !== id))

      // 成功通知
      toast.success('支出を削除しました')
    } catch (error: any) {
      console.error('Failed to delete expense:', error)

      // エラー通知（UIは変更されない）
      notifyError(error, '支出の削除に失敗しました')
    }
  }

  // フックの戻り値
  return {
    expenses,
    addExpense,
    addExpenses,
    updateExpense,
    deleteExpense,
    isLoaded,
  }
}
