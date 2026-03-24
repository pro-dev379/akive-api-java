FROM eclipse-temurin:21-jre

WORKDIR /app
COPY build/libs/*.jar akive-api-java.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "akive-api-java.jar"]