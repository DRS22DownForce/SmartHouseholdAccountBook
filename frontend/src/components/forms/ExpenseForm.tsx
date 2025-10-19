/** @jsxImportSource @emotion/react */
import { css } from "@emotion/react";
import { type ExpenseFormProps } from "./ExpenseFormProps";
import { type ExpenseFormData } from "./ExpenseFormData";
import { useState } from "react";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import { CATEGORIES } from "../shared/constants";
import Typography from "@mui/material/Typography";

const rowStyle = css`
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  @media (max-width: 600px) {
    flex-direction: column;
    align-items: stretch;
  }
`;

export default function ExpenseForm({
    initialData = {},
    error = null,
    onChange,
}: ExpenseFormProps) {
    const [ExpenseFormData, setExpenseFormData] = useState<ExpenseFormData>({
        date: initialData.date || '',
        category: initialData.category || '',
        amount: initialData.amount || 0,
        description: initialData.description || '',
    });

    // フォームデータが変更された時に親コンポーネントに通知
    const handleChange = (newData: ExpenseFormData) => {
        setExpenseFormData(newData);
        onChange(newData); // 親コンポーネントに変更を通知
    };

    return (
        <div css={rowStyle}>
            <TextField
                label="日付"
                type="date"
                value={ExpenseFormData.date}
                onChange={(e) => handleChange({ ...ExpenseFormData, date: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
                fullWidth
                margin="normal"
            />
            <TextField
                label="カテゴリ"
                select
                value={ExpenseFormData.category}
                onChange={(e) => handleChange({ ...ExpenseFormData, category: e.target.value })}
                required
                fullWidth
                margin="normal"
            >
                {CATEGORIES.map(cat => (
                    <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                ))}
            </TextField>
            <TextField
                label="金額"
                type="number"
                value={ExpenseFormData.amount}
                onChange={(e) => handleChange({ ...ExpenseFormData, amount: Number(e.target.value) })}
                required
                inputProps={{ min: 1 }}
                fullWidth
                margin="normal"
            />
            <TextField
                label="説明"
                value={ExpenseFormData.description}
                onChange={(e) => handleChange({ ...ExpenseFormData, description: e.target.value })}
                size="small"
                fullWidth
                margin="normal"
            />
            {error && (
                <Typography color="error" sx={{ mt: 1 }}>{error}</Typography>
            )}
        </div>
    );
}

