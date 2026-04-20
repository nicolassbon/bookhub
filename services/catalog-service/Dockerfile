FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml pom.xml
COPY services/catalog-service/pom.xml services/catalog-service/pom.xml
COPY services/catalog-service/src services/catalog-service/src

RUN mvn -f services/catalog-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/services/catalog-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
