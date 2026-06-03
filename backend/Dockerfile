FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -U -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -U -Dmaven.wagon.http.retryHandler.count=5 clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# docker compose up: want to start the existing containers again.
# docker compose up --build: changed backend code, pom.xml, Dockerfile and want to rebuild the backend image.
# docker compose down: want to stop and remove the existing containers, networks, volumes, and images created by docker compose up.
# docker compose down -v: including the named (db) volumes.