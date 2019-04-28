FROM openjdk:8-jre-alpine3.9
EXPOSE 9080 9081

COPY ./target/external-connector-*.jar external-connector.jar
COPY ./application.yml application.yml

RUN chmod +x external-connector.jar
ENTRYPOINT ["java", "-jar", "external-connector.jar", "server", "application.yml"]