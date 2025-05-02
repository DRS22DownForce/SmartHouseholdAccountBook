-- データベースの作成（もしなければ）
CREATE DATABASE IF NOT EXISTS demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- データベースの選択
USE demo;

-- セッションの文字コードをUTF-8に設定
SET NAMES utf8mb4;

-- 家計簿テーブルの作成
CREATE TABLE IF NOT EXISTS household_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- サンプルデータの挿入
INSERT INTO household_accounts (date, category, amount, description) VALUES
('2024-03-01', '食費', 3500, 'スーパーでの買い物'),
('2024-03-02', '交通費', 1200, '電車賃'),
('2024-03-03', '光熱費', 8500, '電気代'),
('2024-03-04', '住居費', 80000, '3月分家賃'),
('2024-03-05', 'その他', 1500, 'コンビニでの買い物'); 