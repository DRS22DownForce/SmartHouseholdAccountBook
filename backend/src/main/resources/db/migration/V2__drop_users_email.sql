-- users テーブルから email 列を削除（PII を DB に保存しない方針）
ALTER TABLE users DROP COLUMN email;
