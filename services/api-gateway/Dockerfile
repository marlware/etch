# Build from the repository root: docker build -f services/api-gateway/Dockerfile .

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared shared
COPY services services

RUN mvn -q -pl services/api-gateway -am -DskipTests package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S etch && adduser -S etch -G etch
WORKDIR /app
COPY --from=build /workspace/services/api-gateway/target/api-gateway.jar app.jar
USER etch

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
