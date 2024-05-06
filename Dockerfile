FROM openjdk:17-jdk-slim
WORKDIR /app
COPY "/target/task-0.0.1-SNAPSHOT.jar" "/app/task-0.0.1-SNAPSHOT.jar"
CMD ["java", "-jar", "/app/task-0.0.1-SNAPSHOT.jar"]