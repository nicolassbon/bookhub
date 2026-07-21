FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml pom.xml
COPY checkstyle.xml services/identity-service/checkstyle.xml
COPY services/identity-service/pom.xml services/identity-service/pom.xml
COPY services/identity-service/src services/identity-service/src

RUN mvn -f services/identity-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/services/identity-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
