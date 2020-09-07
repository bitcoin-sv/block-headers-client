FROM adoptopenjdk/openjdk11:alpine-slim
COPY build/libs/headerSV-0.0.1.jar /headerSV.jar
COPY wait-for-it.sh .
RUN chmod +x /wait-for-it.sh
RUN apk add --no-cache --upgrade bash
RUN mkdir headersv-data
VOLUME /headersv-data
CMD ["java","-jar","-XshowSettings:vm", "-Djava.security.egd=file:/dev/./urandom", "/headerSV.jar","--stacktrace"]
