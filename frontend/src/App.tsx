import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import ExpenseList from './components/ExpenseList'

function App() {
  return (
    <div className="container-fluid mt-4">
      <h1>スマート家計簿アプリ</h1>
      <ExpenseList />
    </div>
  )
}

export default App
