/** @jsxImportSource @emotion/react */
import { useState, useEffect } from 'react';//useState, useEffectをインポート
//生成されたAPIクライアントと型をインポート
import { DefaultApi, type ExpenseDto } from '../api/generated/api';//実行時には消えるinterface or typeであることを示すためにtypeを使用
import { Configuration } from '../api/generated/configuration'; //Configurationをインポート
//APIクライアントのインスタンスを作成
const api = new DefaultApi(new Configuration({
    basePath: ''
}));

// MUIのコンポーネントをインポート
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import DeleteIcon from '@mui/icons-material/Delete';
import ExpenseForm from './ExpenseForm';
import ExpenseDeleteDialog from './ExpenseDeleteDialog';
import { css } from '@emotion/react';

// レスポンシブなテーブル用スタイル
const tableResponsiveStyle = css`
  width: 100%;
  overflow-x: auto;
  @media (max-width: 600px) {
    th, td {
      min-width: 100px;
      font-size: 0.95rem;
    }
  }
`;

//ExpenseListという関数コンポーネント(関数で定義されたReactのUIコンポーネント)の作成
const ExpenseList = () => {
    const [expenseDtos, setExpenses] = useState<ExpenseDto[]>([]); //支出データを管理するためのステート
    const [error, setError] = useState<string | null>(null); //エラーメッセージを管理するためのステート

    const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null); //削除対象のIDを管理するためのステート

    //useEffectはReactのフック(副作用(コンポーネント外部に影響を及ぼす処理)を扱う関数)で、コンポーネントがマウント(画面に初めて表示)された時に一度だけ実行される
    useEffect(() => {
        fetchExpenses()
    }, []);

    // 支出一覧を取得する関数
    const fetchExpenses = () => {
        api.apiExpensesGet().then((response) => {
            setExpenses(response.data);
        }).catch((error) => {
            setError(error.message);
        });
    };

    // 削除処理
    const handleDelete = (id: number) => {
        api.apiExpensesIdDelete(id)
            .then(() => {
                // 削除成功時はローカルのリストから該当データを除外
                //prevは前のステートの値を参照するための変数
                //prev.filter((e) => e.id !== id)は、前のステートの値を参照して、idが一致するデータを除外した新しい配列を作成
                setExpenses((prev) => prev.filter((e) => e.id !== id));
                setDeleteTargetId(null); // ダイアログを閉じる
            })
            .catch((error) => {
                setError(error.message);
                setDeleteTargetId(null); // ダイアログを閉じる
            });
    };

    return (
        <Box sx={{ mt: 4, px: { xs: 0.5, sm: 2 } }}> {/* スマホ時は左右パディングを減らす */}
            <ExpenseForm onAdded={fetchExpenses} />
            {/* タイトル：
        Typographyでh1相当の表示
        variant="h4"は大きなタイトルを表示するためのオプション
        align="center"は中央揃え
        gutterBottomは下部のマージンを設定*/}
            <Typography variant="h4" align="center" gutterBottom>
                支出リスト
            </Typography>

            {/* エラー表示がある場合だけ表示する */}
            {error && (
                <Typography variant="h6" color="error" align="center" gutterBottom>
                    {error}
                </Typography>
            )}
            {/* テーブルをレスポンシブにするためEmotionのスタイルを適用 */}
            <TableContainer component={Paper} css={tableResponsiveStyle} sx={{ maxHeight: 440 }}>
                {/* stickyHeaderはテーブルのヘッダーが固定されるようにするオプション */}
                <Table stickyHeader aria-label="支出リストテーブル">
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ minWidth: 100 }}>日付</TableCell>
                            <TableCell sx={{ minWidth: 100 }}>カテゴリ</TableCell>
                            <TableCell align="right" sx={{ minWidth: 100 }}>金額</TableCell>
                            <TableCell sx={{ minWidth: 120 }}>説明</TableCell>
                            <TableCell sx={{ minWidth: 80 }}>操作</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {expenseDtos.map((expenseDto) => (
                            <TableRow key={expenseDto.id} hover>
                                <TableCell>{expenseDto.date}</TableCell>
                                <TableCell>{expenseDto.category}</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold' }}>{expenseDto.amount}円</TableCell>
                                <TableCell>{expenseDto.description}</TableCell>
                                <TableCell>
                                    <IconButton onClick={() => setDeleteTargetId(expenseDto.id ?? null)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* 削除確認ダイアログ */}
            <ExpenseDeleteDialog
                open={!!deleteTargetId}
                onClose={() => setDeleteTargetId(null)}
                onDelete={() => deleteTargetId && handleDelete(deleteTargetId!)}
            />
        </Box>
    );
};

//他のファイルからこのコンポーネントを使えるようにする
export default ExpenseList;
