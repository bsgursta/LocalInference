# Stage 1: Build
FROM dhi.io/eclipse-temurin:25-jdk-alpine-dev AS builder

WORKDIR /build

# apk for Alpine
RUN apk add maven

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -P prod

COPY src ./src
RUN mvn clean package -P prod -DskipTests



# Stage 2: Runtime
FROM dhi.io/eclipse-temurin:25-alpine

WORKDIR /server

COPY --from=builder /build/target/localinference-0.0.1.jar app.jar

USER nonroot

EXPOSE 8080 9000

CMD ["java", "-jar", "/server/app.jar"]