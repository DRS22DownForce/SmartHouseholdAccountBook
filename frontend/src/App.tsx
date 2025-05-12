import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import ExpenseList from './components/ExpenseList'

function App() {
  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-12" style={{ maxWidth: '1200px' }}>
          <h1 className="text-center mb-4">スマート家計簿アプリ</h1>
          <ExpenseList />
        </div>
      </div>
    </div>
  )
}

export default App
