# Compila com Maven dentro do Docker: o host não precisa de JDK.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Os testes precisam de um Postgres; rode-os antes com ./mvnw test.
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build --chown=1000:1000 /src/target/quarkus-app/ /app/
USER 1000
EXPOSE 8080
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-jar", "/app/quarkus-run.jar"]
