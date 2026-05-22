#Build
FROM gradle:jdk21-jammy AS builder

WORKDIR /app


COPY . .


RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

ADD https://github.com/grafana/pyroscope-java/releases/download/v0.12.0/pyroscope.jar /app/pyroscope.jar

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/pyroscope.jar pyroscope.jar


EXPOSE 8080


ENTRYPOINT ["java", "-javaagent:/app/pyroscope.jar", "-jar", "app.jar"]