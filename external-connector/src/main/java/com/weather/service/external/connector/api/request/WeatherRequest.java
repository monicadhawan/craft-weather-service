package com.weather.service.external.connector.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class WeatherRequest implements Serializable {

    @JsonProperty
    private String date;

    @JsonProperty
    private String time;

    @JsonProperty
    private int hour;

    @JsonProperty
    private double lat;

    @JsonProperty
    private double longitude;

    @JsonProperty
    private float wind;

    @JsonProperty
    private float temperature;
}
