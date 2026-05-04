-- ========================================
-- 初回スキーマ（users, expenses, monthly_reports）
-- エンティティと一致するテーブル定義
-- ========================================

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cognito_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_cognito_sub (cognito_sub)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    description VARCHAR(255) NOT NULL,
    amount INT NOT NULL,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_expenses_user_id_and_date (user_id, date),
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE monthly_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_month VARCHAR(7) NOT NULL,
    summary TEXT NOT NULL,
    suggestions_json TEXT NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_monthly_reports_user_id_month (user_id, report_month),
    CONSTRAINT fk_monthly_reports_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
