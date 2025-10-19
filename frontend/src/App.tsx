import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import ExpenseList from './components/ExpenseList'

import { Amplify } from 'aws-amplify';
import awsExports from './aws-exports'; 
import { withAuthenticator, type WithAuthenticatorProps } from '@aws-amplify/ui-react';

Amplify.configure(awsExports);

// 認証後のメイン画面
function App({ signOut, user }: WithAuthenticatorProps) {
  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-12" style={{ maxWidth: '1200px' }}>
          <h1 className="text-center mb-4">スマート家計簿アプリ</h1>
          <p>こんにちは、{user?.username} さん</p>
          <button className="btn btn-secondary mb-3" onClick={signOut}>サインアウト</button>
          <ExpenseList />
        </div>
      </div>
    </div>
  )
}

//認証必須のメイン画面を作成
export default withAuthenticator(App);
