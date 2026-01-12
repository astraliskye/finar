FROM maven:3-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY . .

RUN mvn package

FROM eclipse-temurin:21-jre-alpine AS production

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /app/target/*.jar ./app.jar

USER spring:spring

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
