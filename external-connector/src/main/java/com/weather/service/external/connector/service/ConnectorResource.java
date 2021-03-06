package com.weather.service.external.connector.service;

import com.weather.service.external.connector.service.request.WeatherRequest;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ConnectorResource {

    private ConnectorService connectorService;

    public ConnectorResource(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    @POST
    public void getWeatherReading(@Valid final WeatherRequest weatherRequest) throws IOException {
        connectorService.saveWeatherRecord(weatherRequest);
    }
}
