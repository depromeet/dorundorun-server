# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 앱 JAR 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# [signoz 추가시] OpenTelemetry Java Agent 다운로드
# ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

EXPOSE 8080

# signoz on
# ENTRYPOINT ["java","-javaagent:/otel/opentelemetry-javaagent.jar","-Dotel.service.name=sixpack","-Dotel.exporter.otlp.endpoint=http://signoz-otel-collector:4317", "-Dotel.exporter.otlp.protocol=grpc", "-Dotel.metrics.exporter=otlp","-Dotel.traces.exporter=otlp","-Dotel.logs.exporter=otlp","-jar","/app/app.jar"]
ENTRYPOINT ["java","-jar","/app/app.jar"]
