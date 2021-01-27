# Stage 1 - the build process
FROM gradle:6.8.1-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

# Stage 2 - the production environment
FROM openjdk:11-jre-slim
WORKDIR /app
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
ENTRYPOINT ["sh","-c","java -jar /headerSV.jar"]