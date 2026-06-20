#!/bin/bash
# MySQL 初回起動時のみ実行（/docker-entrypoint-initdb.d）
# root で flyway / app 専用ユーザーを作成し、最小権限を付与する
# MySQL 公式イメージは .sh を source するため、set -u などの shell option を変更しない。
# 代わりに必要な環境変数だけを明示的に検証する。
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${MYSQL_DATABASE:?MYSQL_DATABASE is required}"
: "${MYSQL_FLYWAY_USER:?MYSQL_FLYWAY_USER is required}"
: "${MYSQL_FLYWAY_PASSWORD:?MYSQL_FLYWAY_PASSWORD is required}"
: "${MYSQL_APP_USER:?MYSQL_APP_USER is required}"
: "${MYSQL_APP_PASSWORD:?MYSQL_APP_PASSWORD is required}"

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<EOSQL
CREATE USER IF NOT EXISTS '${MYSQL_FLYWAY_USER}'@'%' IDENTIFIED BY '${MYSQL_FLYWAY_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
  ON \`${MYSQL_DATABASE}\`.*
  TO '${MYSQL_FLYWAY_USER}'@'%';

CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'%' IDENTIFIED BY '${MYSQL_APP_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE
  ON \`${MYSQL_DATABASE}\`.*
  TO '${MYSQL_APP_USER}'@'%';

FLUSH PRIVILEGES;
EOSQL
