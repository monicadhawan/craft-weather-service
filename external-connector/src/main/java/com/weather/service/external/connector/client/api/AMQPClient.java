package com.weather.service.external.connector.client.api;

import java.io.Serializable;

public interface AMQPClient {

    void sendMessage(Serializable record);
}
