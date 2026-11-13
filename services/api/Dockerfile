FROM eclipse-temurin:11-jdk-jammy AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && SPRING_PROFILES_ACTIVE=test ./gradlew --no-daemon --max-workers=2 build

FROM eclipse-temurin:11-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
