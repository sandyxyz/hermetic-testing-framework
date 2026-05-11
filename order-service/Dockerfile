FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_FILE=target/order-service-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
