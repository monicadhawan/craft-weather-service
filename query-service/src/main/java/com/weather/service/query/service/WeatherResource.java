package com.weather.service.query.service;

import com.codahale.metrics.annotation.Timed;
import com.weather.service.query.data.dao.TemperatureDAO;
import com.weather.service.query.service.response.GlobalTemperature;
import com.weather.service.query.service.response.HourlyTemperature;
import com.weather.service.query.service.response.Temperature;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Path("/temperature")
@Produces(MediaType.APPLICATION_JSON)
@Builder
@AllArgsConstructor
public class WeatherResource {

    private final TemperatureDAO temperatureDAO;
    private SimpleDateFormat formatter;

    @GET
    @Timed
    @UnitOfWork
    public Temperature getTemperature(
            @QueryParam("lat") final Double latitude,
            @QueryParam("long") final Double longitude,
            @QueryParam("date") final String date) {
        try {
            return temperatureDAO.findByCityMinMax(latitude,
                    longitude,
                    formatter.parse(date),
                    () -> new BadRequestException("Weather data not found"));
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format");
        }
    }

    @GET
    @Path("/hourly")
    @Timed
    @UnitOfWork
    public HourlyTemperature getHourlyTemperature(
            @QueryParam("lat") final Double latitude,
            @QueryParam("long") final Double longitude,
            @QueryParam("date") final String date) {
        try {
            return temperatureDAO.findByCityHourly(latitude,
                    longitude,
                    formatter.parse(date),
                    () -> new BadRequestException("Weather data not found"));
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format");
        }
    }

    @GET
    @Path("/global")
    @Timed
    @UnitOfWork
    public List<GlobalTemperature> getGlobalTemperature(
            @QueryParam("fromDate") final String fromDate,
            @QueryParam("toDate") final String toDate,
            @QueryParam("type") final String type,
            @QueryParam("limit") final int limit) {
        try {
            return temperatureDAO.findGlobalTemperature(formatter.parse(fromDate),
                    formatter.parse(toDate),
                    limit,
                    type,
                    () -> new BadRequestException("Weather data not found"));
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format");
        }
    }
}
