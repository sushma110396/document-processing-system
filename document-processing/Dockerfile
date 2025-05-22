FROM openjdk:17-jdk-alpine
WORKDIR /app

COPY . .

RUN ./gradlew build

RUN cp build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
