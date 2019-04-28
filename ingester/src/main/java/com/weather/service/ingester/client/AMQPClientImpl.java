package com.weather.service.ingester.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.weather.service.ingester.client.api.AMQPClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class AMQPClientImpl implements AMQPClient {
    private Channel channel;
    private String queue;

    public String readMessage(Consumer consumer) {
        String message = null;
        try {
            message = channel.basicConsume(this.queue, true, consumer);
        } catch (IOException e) {
            log.error("Unable to consume message");
        }

        return message;
    }
}
