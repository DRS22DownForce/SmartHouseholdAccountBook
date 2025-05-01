-- データベースの選択
USE demo;

-- 家計簿テーブルの作成
CREATE TABLE IF NOT EXISTS household_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- サンプルデータの挿入
INSERT INTO household_accounts (date, category, amount, description) VALUES
('2024-03-01', '食費', 3500, 'スーパーでの買い物'),
('2024-03-02', '交通費', 1200, '電車賃'),
('2024-03-03', '光熱費', 8500, '電気代'),
('2024-03-04', '住居費', 80000, '3月分家賃'),
('2024-03-05', 'その他', 1500, 'コンビニでの買い物'); 