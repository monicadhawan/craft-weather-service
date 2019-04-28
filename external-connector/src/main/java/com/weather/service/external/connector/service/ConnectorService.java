package com.weather.service.external.connector.service;

import com.weather.service.external.connector.service.request.WeatherRequest;
import com.weather.service.external.connector.client.api.AMQPClient;

public class ConnectorService {
    private AMQPClient amqpClient;

    public ConnectorService(AMQPClient amqpClient) {
        this.amqpClient = amqpClient;
    }

    void saveWeatherRecord(WeatherRequest weatherRequest) {
        // more processing if required
        // validation, etc
        amqpClient.sendMessage(weatherRequest);
    }
}
