# Build from the repository root: docker build -f services/notification-service/Dockerfile .

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared shared
COPY services services

RUN mvn -q -pl services/notification-service -am -DskipTests package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S etch && adduser -S etch -G etch
WORKDIR /app
COPY --from=build /workspace/services/notification-service/target/notification-service.jar app.jar
USER etch

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
