# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

RUN chmod +x gradlew

# Download dependencies (cached)
RUN ./gradlew dependencies --no-daemon

# Copy source
COPY src ./src

# Build fat jar
RUN ./gradlew clean shadowJar --no-daemon

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the fat jar
COPY --from=build /app/build/libs/*-all.jar app.jar

ENV APP_ENV=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]