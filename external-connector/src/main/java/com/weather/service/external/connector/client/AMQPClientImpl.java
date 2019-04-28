package com.weather.service.external.connector.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.weather.service.external.connector.client.api.AMQPClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
public class AMQPClientImpl implements AMQPClient{
    private Channel channel;
    private Connection connection;
    private String exchange;
    private String routingKey;

    public void sendMessage(Serializable record) {
        System.out.println(record.toString());
        try {
            channel.basicPublish(exchange, routingKey, null, SerializationUtils
                    .serialize(new ObjectMapper().writeValueAsString(record)));
        } catch (IOException e) {
            log.error("Error occurred while publishing message to queue");
        }
    }
}
