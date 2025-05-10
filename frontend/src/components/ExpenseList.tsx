import { useState, useEffect } from 'react';//useState, useEffectをインポート
//生成されたAPIクライアントと型をインポート
import { DefaultApi, type ExpenseDto } from '../api/generated/api';//実行時には消えるinterface or typeであることを示すためにtypeを使用
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
        <div className="container-fluid mt-4">
            <h1 className="fs-2 text-center">支出リスト</h1>
            {error && <p className="text-danger fs-4 text-center">{error}</p>}
            {/* 横スクロール対応＆幅100% */}
            <div className="d-block w-100" style={{ overflowX: 'auto' }}>
                <table className="table table-striped table-hover w-100 fs-5" style={{ minWidth: "700px" }}>
                    <thead className="table-light">
                        <tr>
                            <th style={{ minWidth: "120px" }}>日付</th>
                            <th style={{ minWidth: "120px" }}>カテゴリ</th>
                            <th className="text-end" style={{ minWidth: "120px" }}>金額</th>
                            <th style={{ minWidth: "200px" }}>説明</th>
                        </tr>
                    </thead>
                    <tbody>
                        {expenseDtos.map((expense) => (
                            <tr key={expense.id}>
                                <td>{expense.date}</td>
                                <td>{expense.category}</td>
                                <td className="text-end fw-bold">{expense.amount}円</td>
                                <td>{expense.description}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

//他のファイルからこのコンポーネントを使えるようにする
export default ExpenseList;
