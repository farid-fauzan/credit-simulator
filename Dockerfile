# --- Build stage ---
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -q clean package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/credit-simulator.jar ./credit-simulator.jar
COPY file_inputs.txt ./file_inputs.txt
ENTRYPOINT ["java", "-jar", "credit-simulator.jar"]
