# ---------- Stage 1: build the jar with Maven + Java 21 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first so dependencies are cached between builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/cloudnotes.jar app.jar

# Render sets PORT automatically; 8080 is the local default
ENV PORT=8080
EXPOSE 8080

# Keep memory usage friendly for Render's free plan (512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
