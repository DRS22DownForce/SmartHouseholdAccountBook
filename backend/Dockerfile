# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY backend/pom.xml .
COPY backend/src ./src
COPY openapi/openapi.yaml ./openapi/openapi.yaml

RUN mvn clean package -DskipTests -Pdocker

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

