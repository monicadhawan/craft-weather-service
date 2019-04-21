package com.weather.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.HashMap;

@Builder
@Getter
@Setter
public class HourlyTemperature {

    @Length(max = 32)
    private String city;

    private Double latitude;

    private Double longitude;

    HashMap<String, Double> hourlyTemperatures;

    @Builder.Default
    @Length(max = 1)
    private String unit = "F";

}
