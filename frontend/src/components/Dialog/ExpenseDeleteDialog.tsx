import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import { getApiClient, withAuthHeader } from '../../api/expenseApi';
import { useState } from 'react';
import Typography from '@mui/material/Typography';

type Props = {
    open: boolean;  
    expenseId: number
    onClose: () => void;
    onDeleted: () => void;
};

const api = getApiClient();
export default function ExpenseDeleteDialog({ open, expenseId, onClose, onDeleted }: Props) {
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    
    const handleDelete = async (expenseId: number) => {
        try {
            setSubmitting(true);
            const options = await withAuthHeader();
            await api.apiExpensesIdDelete(expenseId, options);
            onDeleted();
            onClose();
        } catch (error: any) {
            setError(error.message);
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>本当に削除しますか？</DialogTitle>
            {error && (
                    <Typography color="error" sx={{ mt: 1 }}>{error}</Typography>
                )}
            <DialogActions>
                <Button onClick={onClose} color="primary">
                    キャンセル
                </Button>
                <Button onClick={() => handleDelete(expenseId)} color="error" variant="contained" disabled={submitting}>
                    {submitting ? '削除中...' : '削除'}
                </Button>
            </DialogActions>
        </Dialog>
    )

}