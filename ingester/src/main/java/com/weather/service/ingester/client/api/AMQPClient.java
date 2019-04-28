package com.weather.service.ingester.client.api;

import com.rabbitmq.client.Consumer;

public interface AMQPClient {

    String readMessage(Consumer consumer);
}
