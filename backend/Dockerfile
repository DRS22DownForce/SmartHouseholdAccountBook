# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

#依存関係を事前にダウンロードするコマンド
#Dockerの機能でRUNコマンドまでのレイヤーに変更がない場合はRUNコマンドはスキップされキャッシュを利用する
#pomに変更がない場合はキャッシュが利用され、イメージのビルドが速くなる
COPY backend/pom.xml .
RUN mvn dependency:go-offline

COPY backend/src ./src
COPY openapi/openapi.yaml ./openapi/openapi.yaml

#Mavenの並列ビルドを有効化
RUN mvn clean package -T 1C -DskipTests -Pdocker

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

