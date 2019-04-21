package com.weather.service.external.connector.api;

import com.weather.service.external.connector.api.request.WeatherRequest;
import com.weather.service.external.connector.client.AMQPClient;

import java.io.IOException;

public class ConnectorService {
    private AMQPClient amqpClient;

    public ConnectorService(AMQPClient amqpClient) {
        this.amqpClient = amqpClient;
    }

    public void saveWeatherRecord(WeatherRequest weatherRequest) throws IOException {
        // more processing if required
        // validation
        amqpClient.sendMessage(weatherRequest);
    }

    public void testStringResource(String record) throws IOException {
        amqpClient.sendMessage(record);
    }
}
