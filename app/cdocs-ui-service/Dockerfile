FROM openjdk:10-jre-slim
COPY ./target/cdocs-ui-service-0.0.1-SNAPSHOT.jar /usr/src/hola/
WORKDIR /usr/src/hola
EXPOSE 10000
CMD ["java", "-jar", "cdocs-ui-service-0.0.1-SNAPSHOT.jar"]