package com.weather.service;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class WeatherServiceApplication extends Application<WeatherServiceConfiguration> {
    public static void main(String[] args) throws Exception {
        final String command = args.length > 0 ? args[0] : "server";
        final String applicationYamlFile = args.length > 1 ? args[1] : "application.yml";
        new WeatherServiceApplication().run(command, applicationYamlFile);
    }

    private final HibernateBundle<WeatherServiceConfiguration> hibernate = new HibernateBundle<WeatherServiceConfiguration>(TemperatureEvent.class, Location.class) {
        public DataSourceFactory getDataSourceFactory(WeatherServiceConfiguration configuration) {
            return configuration.getDatabase();
        }
    };


    @Override
    public String getName() {
        return "weather-service";
    }

    @Override
    public void initialize(Bootstrap<WeatherServiceConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(WeatherServiceConfiguration configuration,
                    Environment environment) {
        final TemperatureDAO temperatureDAO = new TemperatureDAO(hibernate.getSessionFactory());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getDefault());
        environment.jersey().register(new WeatherResource(temperatureDAO, formatter));
        environment.getObjectMapper().setDateFormat(formatter);

    }

}
