FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

COPY . .

RUN mvn clean package -DskipTests && \
    rm -rf /root/.m2/repository &&  \
    rm -rf /target/*sources.jar

FROM eclipse-temurin:17-jre-alpine

EXPOSE 5050

COPY --from=build /target/certifica-utf-gateway-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
