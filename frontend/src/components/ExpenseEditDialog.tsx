/** @jsxImportSource @emotion/react */
import { useEffect, useState } from 'react';
import type { ExpenseDto, ExpenseRequestDto } from '../api/generated/api';
import { getApiClient, withAuthHeader } from '../api/expenseApi';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

// APIクライアントのインスタンスを取得
const api = getApiClient();

// カテゴリの例（本番ではマスタAPIや設定から取得するのが望ましい）
const categories = ['食費', '日用品', '交通費', '娯楽', 'その他'];

type Props = {
    open: boolean;
    expense: ExpenseDto | null;
    onClose: () => void;
    onSaved: (updated: ExpenseDto) => void;
};

/**
 * 既存の家計簿データを編集（PUT）するダイアログ
 * 初心者向けポイント:
 * - フォームの状態は useState で管理します
 * - 送信ボタンでAPIのPUTを呼び、成功したら親に結果を返します
 */
export default function ExpenseEditDialog({ open, expense, onClose, onSaved }: Props) {
    const [date, setDate] = useState<string>('');
    const [category, setCategory] = useState<string>('');
    const [amount, setAmount] = useState<number>(0);
    const [description, setDescription] = useState<string>('');
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    // ダイアログが開いた時に初期値をフォームへ反映
    useEffect(() => {
        if (expense) {
            setDate(expense.date ?? '');
            setCategory(expense.category ?? '');
            setAmount(expense.amount ?? 0);
            setDescription(expense.description ?? '');
            setError(null);
        }
    }, [expense, open]);

    const handleSubmit = async () => {
        if (!expense?.id) return;
        setError(null);

        // 簡易バリデーション：必須入力と金額チェック
        if (!date || !category || !Number.isFinite(amount) || amount <= 0) {
            setError('日付・カテゴリ・金額は必須で、金額は1以上にしてください');
            return;
        }

        const req: ExpenseRequestDto = {
            date,
            category,
            amount,
            description,
        };

        try {
            setSubmitting(true);
            const options = await withAuthHeader();
            // 生成されたクライアントのPUTメソッドを呼ぶ
            const res = await api.apiExpensesIdPut(expense.id, req, options);
            onSaved(res.data);
        } catch (e: any) {
            // サーバが共通エラーを返す場合は message を優先表示
            const msg = e?.response?.data?.message || e?.message || '更新に失敗しました';
            setError(msg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>支出を編集</DialogTitle>
            <DialogContent dividers>
                <TextField
                    label="日付"
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    required
                    fullWidth
                    margin="normal"
                />
                <TextField
                    label="カテゴリ"
                    select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    required
                    fullWidth
                    margin="normal"
                >
                    {categories.map((cat) => (
                        <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                    ))}
                </TextField>
                <TextField
                    label="金額"
                    type="number"
                    value={amount}
                    onChange={(e) => setAmount(Number(e.target.value))}
                    required
                    inputProps={{ min: 1 }}
                    fullWidth
                    margin="normal"
                />
                <TextField
                    label="説明"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    fullWidth
                    margin="normal"
                />
                {error && (
                    <Typography color="error" sx={{ mt: 1 }}>{error}</Typography>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={submitting}>キャンセル</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={submitting}>保存</Button>
            </DialogActions>
        </Dialog>
    );
}


