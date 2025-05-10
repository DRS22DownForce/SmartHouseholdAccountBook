import { useState, useEffect } from 'react';//useState, useEffectをインポート
//生成されたAPIクライアントと型をインポート
import { DefaultApi, type ExpenseDto} from '../api/generated/api';//実行時には消えるinterface or typeであることを示すためにtypeを使用
import { Configuration } from '../api/generated/configuration'; //Configurationをインポート
//APIクライアントのインスタンスを作成
const api = new DefaultApi(new Configuration({
    basePath: 'http://localhost:8080'
}));

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
        <div>
            <h1>支出リスト</h1>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <ul>
                {expenseDtos.map((expense) => (
                    <li key={expense.id}>
                        {expense.date} | {expense.category} | {expense.amount}円 | {expense.description}
                    </li>
                ))}
            </ul>
        </div>
    );
};

//他のファイルからこのコンポーネントを使えるようにする
export default ExpenseList;
