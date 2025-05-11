import { useState, useEffect } from 'react';//useState, useEffectをインポート
//生成されたAPIクライアントと型をインポート
import { DefaultApi, type ExpenseDto } from '../api/generated/api';//実行時には消えるinterface or typeであることを示すためにtypeを使用
import { Configuration } from '../api/generated/configuration'; //Configurationをインポート
//APIクライアントのインスタンスを作成
const api = new DefaultApi(new Configuration({
    basePath: 'http://localhost:8080'
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


//ExpenseListという関数コンポーネント(関数で定義されたReactのUIコンポーネント)の作成
const ExpenseList = () => {
    const [expenseDtos, setExpenses] = useState<ExpenseDto[]>([]); //支出データを管理するためのステート
    const [error, setError] = useState<string | null>(null); //エラーメッセージを管理するためのステート

    //useEffectはReactのフック(副作用(コンポーネント外部に影響を及ぼす処理)を扱う関数)で、コンポーネントがマウント(画面に初めて表示)された時に一度だけ実行される
    useEffect(() => {
        api.apiExpensesGet().then((response) => {
            setExpenses(response.data); //取得した支出データをステートに保存
        }).catch((error) => {
            setError(error.message); //エラーが発生した場合、エラーメッセージをステートに保存
        });
    }, []);

    return (
        <Box sx={{ mt: 4, px: 2 }}> {/* ボックスコンテナを作成 mt:4は上部のマージンを4pxに設定 px:2は左右のパディングを2pxに設定 */}
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
            {/*TableContainerはテーブルをラップするコンテナコンポーネント
            Paperはテーブルの背景色を設定するコンポーネント
            sx={{ maxHeight: 440 }}はテーブルの高さを440pxに設定*/}
            {/*sxとはMUIのコンポーネントのスタイルを設定するためのオプション*/}
            <TableContainer component={Paper} sx={{ maxHeight: 440 }}>
                {/* stickyHeaderはテーブルのヘッダーが固定されるようにするオプション */}
                <Table stickyHeader aria-label="支出リストテーブル">
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ minWidth: 120 }}>日付</TableCell>
                            <TableCell sx={{ minWidth: 120 }}>カテゴリ</TableCell>
                            <TableCell align="right" sx={{ minWidth: 120 }}>金額</TableCell>
                            <TableCell sx={{ minWidth: 200 }}>説明</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {expenseDtos.map((expense) => (
                            <TableRow key={expense.id} hover>
                                <TableCell>{expense.date}</TableCell>
                                <TableCell>{expense.category}</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 'bold' }}>{expense.amount}円</TableCell>
                                <TableCell>{expense.description}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>
    );
};

//他のファイルからこのコンポーネントを使えるようにする
export default ExpenseList;
