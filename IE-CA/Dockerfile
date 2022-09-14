FROM maven:3.6.3-jdk-14 AS MAVEN_BUILD

COPY pom.xml /build/

COPY src /build/src/

WORKDIR /build/

RUN mvn package

FROM openjdk:15-alpine

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/*.war /app/app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]