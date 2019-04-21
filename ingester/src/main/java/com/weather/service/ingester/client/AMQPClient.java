package com.weather.service.ingester.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@AllArgsConstructor
public class AMQPClient {
    private Channel channel;
    private Connection connection;
    private String queue;

    public String readMessage(Consumer consumer) throws IOException {
        return channel.basicConsume(this.queue, true, consumer);
    }

    public void close() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
    }
}
