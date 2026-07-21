# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

ARG SERVICE

# Copy reactor descriptors first so dependency resolution remains cacheable.
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY pay-service/pom.xml pay-service/pom.xml
RUN mvn -B -pl "${SERVICE}" -am dependency:go-offline

COPY common/src common/src
COPY ${SERVICE}/src ${SERVICE}/src
RUN mvn -B -pl "${SERVICE}" -am -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

ARG SERVICE
COPY --from=build --chown=spring:spring /workspace/${SERVICE}/target/${SERVICE}-*.jar application.jar

USER spring
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
