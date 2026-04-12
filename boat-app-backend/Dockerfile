# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copy POM first — lets Docker cache the dependency layer separately
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and run full build including tests — CI/CD not yet in place
COPY src ./src
RUN mvn clean package -B

# ─── Stage 2: Run ────────────────────────────────────────────────────────────
# eclipse-temurin:25-jre-alpine — minimal JRE, no JDK tools in production
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Default profile — overridable at any level:
#   docker run -e SPRING_PROFILES_ACTIVE=prod ...
#   docker-compose environment: SPRING_PROFILES_ACTIVE: dev
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

# -Duser.timezone=UTC is mandatory — ensures Instant timestamps are stored/logged in UTC
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]




