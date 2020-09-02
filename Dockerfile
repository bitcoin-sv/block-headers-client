# Docker file for the Listener (multi-stage Build file).
# -----------------------------------------------------------------------------

# Copying the code in the image and packagin with gradle:

FROM gradle:jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/headerSV
WORKDIR /home/gradle/headerSV
RUN gradle assemble

# Preparing the final image, copying the JAR generated in the previous stage.

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder /home/gradle/headerSV/build/libs/headerSV-0.0.1.jar /app.jar
COPY wait-for-it.sh .
RUN apk add --no-cache --upgrade bash
VOLUME /headerSV-data
CMD ["java","-jar","-XX:MaxRAMFraction=1","-XshowSettings:vm", "-Djava.security.egd=file:/dev/./urandom","/app.jar","--stacktrace"]

