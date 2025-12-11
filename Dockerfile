FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .

# mvnw’ye çalıştırma izni veriyoruz (ZORUNLU)
RUN chmod +x mvnw

RUN ./mvnw -q -e -DskipTests package

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
