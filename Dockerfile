# Stage 1 - the build process
FROM gradle:6.8.1-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

# Stage 2 - the production environment
FROM openjdk:18-oraclelinux8
WORKDIR /app
COPY --from=build /home/gradle/src/app/build/libs/*.jar /app/
ENTRYPOINT ["sh","-c","java -jar /app/headersv-app-2.0.2.jar"]

