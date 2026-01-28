FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY apps/api/build/libs/api.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
