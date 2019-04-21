package com.weather.external.connector.weather.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ConnectorConfiguration extends Configuration{
    @Valid
    @NotNull
    @JsonProperty("amqp")
    private AMQPConfiguration amqpConfiguration = new AMQPConfiguration();

    public AMQPConfiguration getAmqpConfiguration() {
        return amqpConfiguration;
    }
}
