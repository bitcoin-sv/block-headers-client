# Docker file for the Listener (multi-stage Build file).
# -----------------------------------------------------------------------------

# Copying the code in the image and packagin with gradle:

FROM gradle:jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/headerSV
WORKDIR /home/gradle/headerSV
RUN gradle assemble

# Preparing the final image, copying the JAR generated in the previous stage.
# We include here and additional step, which is top copy an script ("wait-for-it.sh"), that
# allows us to wait for another service to be ready before we execute our app. This is useful
# when we executre this app from the Docker-compose file.
# Since we include an script in the image, we also install the BASH (another additional step)

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=builder /home/gradle/headerSV/build/libs/headerSV-0.0.1.jar /app.jar
#RUN apk add --no-cache --upgrade bash
VOLUME /headerSV-data
CMD ["java","-jar","-XshowSettings:vm", "-Djava.security.egd=file:/dev/./urandom", "/app.jar","--stacktrace"]
