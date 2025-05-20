FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN mkdir /app/pdf

COPY build/libs/on-data-consumer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
