package com.weather.service.query.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Builder
@Getter
@Setter
public class GlobalTemperature {

    @Length(max = 32)
    private final String city;

    @Length(max = 32)
    private final float temperature;

    private final Double latitude;

    private final Double longitude;

    @Length(max = 1)
    @Builder.Default
    private String unit = "F";


}
