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
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
