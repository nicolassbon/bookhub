FROM eclipse-temurin:21-jre

WORKDIR /app

# Initial scaffold image.
# The service artifact is not wired yet in this phase.
COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
