package com.weather.external.connector.weather.api;

import com.weather.external.connector.weather.client.AMQPClient;

import java.io.IOException;

public class ConnectorService {
    private AMQPClient amqpClient;

    public ConnectorService(AMQPClient amqpClient) {
        this.amqpClient = amqpClient;
    }

    public void saveWeatherRecord(WeatherJson weatherJson) throws IOException {
        // more processing if required
        // validation
        amqpClient.sendMessage(weatherJson);
    }

    public void testStringResource(String record) throws IOException {
        amqpClient.sendMessage(record);
    }
}
