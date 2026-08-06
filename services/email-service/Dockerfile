# Build from the repository root: docker build -f services/email-service/Dockerfile .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared shared
COPY services services

RUN mvn -q -pl services/email-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S etch && adduser -S etch -G etch
WORKDIR /app
COPY --from=build /workspace/services/email-service/target/email-service.jar app.jar
USER etch

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
