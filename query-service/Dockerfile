FROM openjdk:8-jre-alpine3.9

EXPOSE 8080 8081

COPY ./target/query-service*.jar query-service.jar
COPY ./application.yml application.yml

RUN chmod +x query-service.jar
ENTRYPOINT ["java", "-jar", "query-service.jar", "server", "application.yml"]
