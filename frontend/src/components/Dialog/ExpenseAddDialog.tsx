import { useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import type { ExpenseDto, ExpenseRequestDto } from '../../api/generated/api';
import { getApiClient, withAuthHeader } from '../../api/expenseApi';
import ExpenseForm from '../forms/ExpenseForm';
import { type ExpenseFormData } from '../forms/ExpenseFormData';

const api = getApiClient();

type Props = {
  open: boolean;
  onClose: () => void;
  // 追加が成功したときに親へ返す（一覧に即時反映するため）
  onSaved: (created: ExpenseDto) => void;
};

export default function ExpenseAddDialog({ open, onClose, onSaved }: Props) {
  // 入力中のフォーム状態
  const [form, setForm] = useState<ExpenseFormData>({
    date: '',
    category: '',
    amount: 0,
    description: '',
  });
  // エラーメッセージ表示用
  const [error, setError] = useState<string | null>(null);
  // 送信中フラグ（多重送信防止）
  const [submitting, setSubmitting] = useState(false);

  // フォームの変更を受け取る
  const handleFormChange = (data: ExpenseFormData) => {
    setForm(data);
    setError(null);
  };

  // 保存（追加）処理
  const handleSave = async () => {
    setError(null);

    // 簡易バリデーション（初心者向け：最低限のチェック）
    if (!form.date || !form.category || !Number.isFinite(form.amount) || form.amount <= 0) {
      setError('日付・カテゴリ・金額は必須で、金額は1以上にしてください');
      return;
    }

    const req: ExpenseRequestDto = {
      date: form.date,
      category: form.category,
      amount: form.amount,
      description: form.description,
    };

    try {
      setSubmitting(true);
      const options = await withAuthHeader(); // セキュアにAPI呼び出し
      const res = await api.apiExpensesPost(req, options); // 追加API呼び出し
      onSaved(res.data); // 親へ結果を返して一覧に反映
      onClose(); // ダイアログを閉じる
    } catch (e: any) {
      const msg = e?.response?.data?.message || e?.message || '追加に失敗しました';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>支出を追加</DialogTitle>
      <DialogContent dividers>
        <ExpenseForm
          initialData={form}
          error={error}
          onChange={handleFormChange}
        />
        {error && (
          <Typography color="error" sx={{ mt: 1 }}>{error}</Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>キャンセル</Button>
        <Button onClick={handleSave} variant="contained" disabled={submitting}>
          {submitting ? '保存中...' : '保存'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}