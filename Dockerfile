FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM openjdk:11-ea-17-jre-slim
RUN groupadd appuser && useradd appuser -g appuser
WORKDIR /app
COPY --from=builder --chown=appuser:appuser /app/target/backend-app.jar app.jar
EXPOSE 8195
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]