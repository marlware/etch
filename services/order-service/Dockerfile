# Build from the repository root: docker build -f services/order-service/Dockerfile .
# The reactor's root pom.xml lists every module, so Maven needs the whole
# source tree present even though this image only ships order-service.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
COPY shared shared
COPY services services

RUN mvn -q -pl services/order-service -am -DskipTests package

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S etch && adduser -S etch -G etch
WORKDIR /app
COPY --from=build /workspace/services/order-service/target/order-service.jar app.jar
USER etch

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
