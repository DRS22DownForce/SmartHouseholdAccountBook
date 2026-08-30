# --- Build stage ---
# JDK とネットワークは公式 Maven イメージ。実際のビルドコマンドはプロジェクトの Maven Wrapper でバージョン固定。
FROM maven:3.9.15-eclipse-temurin-25 AS builder
WORKDIR /app

COPY backend/mvnw backend/mvnw.cmd ./
COPY backend/.mvn .mvn
RUN chmod +x mvnw

#依存関係を事前にダウンロードするコマンド
#Dockerの機能でRUNコマンドまでのレイヤーに変更がない場合はRUNコマンドはスキップされキャッシュを利用する
#pomに変更がない場合はキャッシュが利用され、イメージのビルドが速くなる
COPY backend/pom.xml .
RUN ./mvnw -B dependency:go-offline

COPY backend/src ./src
COPY openapi ./openapi

#Mavenの並列ビルドを有効化（-B: バッチモードでログを簡潔に）
RUN ./mvnw -B clean package -T 1C -DskipTests -Pdocker

# --- Runtime stage ---
FROM eclipse-temurin:25-jre

# healthcheck 用 wget と非 root 実行ユーザー
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /nonexistent --no-create-home app

WORKDIR /app

# コピー時に JAR の所有者を app に設定
COPY --from=builder --chown=app:app /app/target/app.jar app.jar

USER app
# このコンテナ内のアプリケーションが 8080 ポートをリッスンすることを明示
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
