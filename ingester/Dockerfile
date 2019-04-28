FROM openjdk:8-jre-alpine3.9

COPY ./target/ingester-*.jar ingester-service.jar
COPY ./application.yml application.yml

RUN chmod +x ingester-service.jar
ENTRYPOINT ["java", "-jar", "ingester-service.jar", "server", "application.yml"]