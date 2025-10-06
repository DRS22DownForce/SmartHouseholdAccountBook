/** @jsxImportSource @emotion/react */
import { useEffect, useState } from 'react';
import type { ExpenseDto, ExpenseRequestDto } from '../../api/generated/api';
import { getApiClient, withAuthHeader } from '../../api/expenseApi';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import { type ExpenseFormData } from '../forms/ExpenseFormData';
import ExpenseForm from '../forms/ExpenseForm';

const api = getApiClient();

type Props = {
    open: boolean;
    expense: ExpenseDto | null;
    onClose: () => void;
    onSaved: (updated: ExpenseDto) => void;
};

export default function ExpenseEditDialog({ open, expense, onClose, onSaved }: Props) {
    const [ExpenseFormData, setExpenseFormData] = useState<ExpenseFormData>({
        date: '',
        category: '',
        amount: 0,
        description: '',
    });
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (expense) {
            setExpenseFormData({
                date: expense.date ?? '',
                category: expense.category ?? '',
                amount: expense.amount ?? 0,
                description: expense.description ?? '',
            });
            setError(null);
        }
    }, [expense, open]);

    // フォームデータが変更されたときの処理
    const handleFormChange = (data: ExpenseFormData) => {
        setExpenseFormData(data);
        setError(null); // エラーをクリア
    };

    // 保存処理
    const handleSave = async () => {
        if (!expense?.id){
            setError('支出が見つかりません');
            return;
        }
        setError(null);

        // 簡易バリデーション
        if (!ExpenseFormData.date || !ExpenseFormData.category || !Number.isFinite(ExpenseFormData.amount) || ExpenseFormData.amount <= 0) {
            setError('日付・カテゴリ・金額は必須で、金額は1以上にしてください');
            return;
        }

        const req: ExpenseRequestDto = {
            date: ExpenseFormData.date,
            category: ExpenseFormData.category,
            amount: ExpenseFormData.amount,
            description: ExpenseFormData.description,
        };

        try {
            setSubmitting(true);
            const options = await withAuthHeader();
            const res = await api.apiExpensesIdPut(expense.id, req, options);
            onSaved(res.data);
            onClose();
        } catch (e: any) {
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
                <ExpenseForm
                    initialData={ExpenseFormData}
                    error={error}
                    onChange={handleFormChange}
                />
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