FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml pom.xml
COPY checkstyle.xml checkstyle.xml
COPY checkstyle.xml services/library-service/checkstyle.xml
COPY services/library-service/pom.xml services/library-service/pom.xml
COPY services/library-service/src services/library-service/src

RUN mvn -f services/library-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/services/library-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
