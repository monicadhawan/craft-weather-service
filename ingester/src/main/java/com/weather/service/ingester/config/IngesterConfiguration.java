package com.weather.service.ingester.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class IngesterConfiguration extends Configuration{
    @Valid
    @NotNull
    @JsonProperty("amqp")
    private AMQPConfiguration amqpConfiguration = new AMQPConfiguration();


    @Valid
    @NotNull
    @JsonProperty
    private final DataSourceFactory database = new DataSourceFactory();
}
