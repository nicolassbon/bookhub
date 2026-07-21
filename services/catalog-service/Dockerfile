FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml pom.xml
COPY checkstyle.xml services/catalog-service/checkstyle.xml
COPY services/catalog-service/pom.xml services/catalog-service/pom.xml
COPY services/catalog-service/src services/catalog-service/src

RUN mvn -f services/catalog-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/services/catalog-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
