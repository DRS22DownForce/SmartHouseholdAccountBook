/** @jsxImportSource @emotion/react */
import { useState, useEffect } from 'react';//useState, useEffectをインポート
//生成されたAPIクライアントと型をインポート
import { type ExpenseDto } from '../api/generated/api';//実行時には消えるinterface or typeであることを示すためにtypeを使用
import { getApiClient, withAuthHeader } from '../api/expenseApi';
//APIクライアントのインスタンスを作成
const api = getApiClient();

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
import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';
// import ExpenseForm from './ExpenseForm'; // 追加をダイアログ化するため削除
import ExpenseDeleteDialog from './Dialog/ExpenseDeleteDialog';
import ExpenseEditDialog from './Dialog/ExpenseEditDialog';
import ExpenseAddDialog from './Dialog/ExpenseAddDialog';
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
    const [editTarget, setEditTarget] = useState<ExpenseDto | null>(null); //編集対象
    const [addDialogOpen, setAddOpen] = useState(false); // 追加ダイアログの開閉

    //useEffectはReactのフック(副作用(コンポーネント外部に影響を及ぼす処理)を扱う関数)で、コンポーネントがマウント(画面に初めて表示)された時に一度だけ実行される
    useEffect(() => {
        fetchExpenses()
    }, []);

    // 支出一覧を取得する関数
    const fetchExpenses = async () => {
        try {
            const options = await withAuthHeader();
            const response = await api.apiExpensesGet(options);
            setExpenses(response.data);
        } catch (error: any) {
            setError(error.message);
        }
    };

    return (
        <Box sx={{ mt: 4, px: { xs: 0.5, sm: 2 } }}> {/* スマホ時は左右パディングを減らす */}
            {/* 追加ボタン：押すと追加ダイアログを開く */}
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
                    支出を追加
                </Button>
            </Box>

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
                                    <IconButton onClick={() => setEditTarget(expenseDto)} sx={{ mr: 1 }}>
                                        {/* 編集アイコンは標準で用意されていないためテキストでも可 */}
                                        ✏️
                                    </IconButton>
                                    <IconButton onClick={() => setDeleteTargetId(expenseDto.id ?? null)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* 追加ダイアログ */}
            <ExpenseAddDialog
                open={addDialogOpen}
                onClose={() => setAddOpen(false)}
                onSaved={(created) => {
                    setAddOpen(false);
                    // 一覧の先頭に差し込む（最小再描画で高速）
                    setExpenses(prev => [created, ...prev]);
                }}
            />

            {/* 削除確認ダイアログ */}
            <ExpenseDeleteDialog
                open={!!deleteTargetId}
                expenseId={deleteTargetId ?? 0}
                onClose={() => setDeleteTargetId(null)}
                onDeleted={fetchExpenses}
            />

            {/* 編集ダイアログ */}
            <ExpenseEditDialog
                open={!!editTarget}
                expense={editTarget}
                onClose={() => setEditTarget(null)}
                onSaved={(updated) => {
                    setEditTarget(null)
                    // 一覧の該当行だけ差し替える（最小再描画）
                    setExpenses(prev => prev.map(x => x.id === updated.id ? updated : x))
                }}
            />
        </Box>
    );
};

//他のファイルからこのコンポーネントを使えるようにする
export default ExpenseList;