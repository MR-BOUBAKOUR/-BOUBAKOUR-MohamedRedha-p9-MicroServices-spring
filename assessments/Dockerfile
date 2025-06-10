# --------------------------------------------------------------
# 🔨 BUILD STAGE
# --------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mvn package -DskipTests

# --------------------------------------------------------------
# 🚀 RUNTIME STAGE
# --------------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN useradd -r -u 1001 -g root appuser && \
    chown appuser:root app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
