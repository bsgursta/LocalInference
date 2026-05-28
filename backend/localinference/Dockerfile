# Dev Docker Container
FROM dhi.io/eclipse-temurin:25-jdk-alpine3.22-dev

WORKDIR /server

EXPOSE 8080 9000

CMD ["/server/./mvnw", "spring-boot:run", "-P", "dev"]