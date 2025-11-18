FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copia tudo (incluindo pom.xml, mvnw, .mvn, src/)
COPY . .

# Faz o build do jar
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o jar do estágio anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
