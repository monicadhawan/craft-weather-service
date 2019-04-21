package com.weather.service.external.connector.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

@AllArgsConstructor
public class AMQPClient {
    private Channel channel;
    private Connection connection;
    private String exchange;
    private String routingKey;

    public void sendMessage(Serializable record) throws IOException {
        channel.basicPublish(exchange, routingKey, null, SerializationUtils.serialize(record));
    }

    public void close() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
    }
}
