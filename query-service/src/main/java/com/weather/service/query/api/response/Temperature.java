package com.weather.service.query.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Temperature {

    @Length(max = 32)
    private final String city;

    @Length(max = 32)
    private final float temperature;

    private final Date observedTime;

    private final Double latitude;

    private final Double longitude;

    private float max;

    private float min;

    @Length(max = 1)
    @Builder.Default
    private String unit = "F";

}
