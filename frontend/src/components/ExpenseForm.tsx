/** @jsxImportSource @emotion/react */
import { useState } from 'react';
import { DefaultApi, type ExpenseRequestDto } from '../api/generated/api';
import { Configuration } from '../api/generated/configuration';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import MenuItem from '@mui/material/MenuItem';
import Typography from '@mui/material/Typography';
import { css } from '@emotion/react';

// APIクライアントのインスタンス
const api = new DefaultApi(new Configuration({ basePath: '' }));

// カテゴリの例
const categories = ['食費', '日用品', '交通費', '娯楽', 'その他'];

type Props = {
  onAdded: () => void; //追加成功時に呼び出されるコールバック関数
}

// レスポンシブなフォーム用スタイル
const formResponsiveStyle = css`
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;

  @media (max-width: 600px) {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
    > * {
      width: 100% !important;
      min-width: 0 !important;
    }
  }
`;

const ExpenseForm = ({ onAdded }: Props) => {
  const [date, setDate] = useState<string>('');
  const [category, setCategory] = useState<string>('');
  const [amount, setAmount] = useState<number>(0);
  const [description, setDescription] = useState<string>('');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!date || !category || !amount || !description) {
      setError('日付・カテゴリ・金額・説明は必須です');
      return;
    }

    if (isNaN(amount) || amount <= 0) {
      setError('金額は正の数値で入力してください');
      return;
    }

    const req: ExpenseRequestDto = {
      date: date,
      category: category,
      amount: amount,
      description: description,
    };

    try {
      await api.apiExpensesPost(req); //api.apiExpensesPostは非同期関数なのでawaitを付けて結果が返ってくるまで待つ
      //追加成功時はフォームをクリアし、リストを更新
      setDate('');
      setCategory('');
      setAmount(0);
      setDescription('');
      onAdded();
    } catch (error: any) {
      setError(error.message || 'エラーが発生しました');
    }
  }

  return (
    <form onSubmit={handleSubmit} css={formResponsiveStyle}>
      <TextField
        label="日付"
        type="date"
        value={date}
        onChange={e => setDate(e.target.value)}
        InputLabelProps={{ shrink: true }}
        required
        size="small"
      />
      <TextField
        label="カテゴリ"
        select
        value={category}
        onChange={e => setCategory(e.target.value)}
        required
        size="small"
        sx={{ minWidth: 120 }}
      >
        {categories.map(cat => (
          <MenuItem key={cat} value={cat}>{cat}</MenuItem>
        ))}
      </TextField>
      <TextField
        label="金額"
        type="number"
        value={amount}
        onChange={e => setAmount(Number(e.target.value))}
        required
        size="small"
        inputProps={{ min: 1 }}
      />
      <TextField
        label="説明"
        value={description}
        onChange={e => setDescription(e.target.value)}
        size="small"
        required
        sx={{ minWidth: 200 }}
      />
      <Button type="submit" variant="contained" color="primary">
        追加
      </Button>
      {error && (
        <Typography color="error" sx={{ ml: 2 }}>{error}</Typography>
      )}
    </form>
  );
}

export default ExpenseForm;