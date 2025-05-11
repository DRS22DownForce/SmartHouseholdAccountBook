import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

type Props = {
  open: boolean;
  onClose: () => void;
  onDelete: () => void;
};

const ExpenseDeleteDialog = ({ open, onClose, onDelete }: Props) => (
  <Dialog open={open} onClose={onClose}>
    <DialogTitle>本当に削除しますか？</DialogTitle>
    <DialogActions>
      <Button onClick={onClose} color="primary">
        キャンセル
      </Button>
      <Button onClick={onDelete} color="error" variant="contained">
        削除
      </Button>
    </DialogActions>
  </Dialog>
);

export default ExpenseDeleteDialog;