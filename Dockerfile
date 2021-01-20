FROM openjdk:11-jre
COPY build/libs/headerSV.jar /headerSV.jar
CMD ["java", "-jar", "/headerSV.jar"]