package com.weather.external.connector.weather.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@Data
public class AMQPConfiguration {
    @JsonProperty
    private String exchange = "app.weather.data";

    @JsonProperty
    private String queue = "app.weather.sensors";

    @JsonProperty
    private String routingKey = queue;

    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1)
    @Max(65535)
    @JsonProperty
    private int port = ConnectionFactory.DEFAULT_AMQP_PORT;
}
