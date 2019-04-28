package com.weather.service.query.data.dao;

import com.weather.service.query.service.response.GlobalTemperature;
import com.weather.service.query.service.response.HourlyTemperature;
import com.weather.service.query.service.response.Temperature;
import com.weather.service.query.data.Datehelper;
import com.weather.service.query.data.Location;
import com.weather.service.query.data.Location_;
import com.weather.service.query.data.TemperatureEvent;
import com.weather.service.query.data.TemperatureEvent_;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.WebApplicationException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class TemperatureDAO extends AbstractDAO<TemperatureEvent> {

    public TemperatureDAO(SessionFactory factory) {
        super(factory);
    }

    public Temperature findByCityMinMax(Double latitude, Double longitude, Date date,
                                        Supplier<? extends WebApplicationException> exceptionSupplier) {
        final CriteriaBuilder cb = currentSession().getCriteriaBuilder();

        // Get current temperature
        CriteriaQuery<Temperature> criteriaQuery = cb.createQuery(Temperature.class);
        Root<TemperatureEvent> from = criteriaQuery.from(TemperatureEvent.class);
        Predicate dateCriteria = cb.lessThanOrEqualTo(from.get(TemperatureEvent_.DATE), date);
        Join location = from.join(TemperatureEvent_.LOCATION);
        Predicate cityCriteria = getLocationCriteria(latitude, longitude, location, cb);
        criteriaQuery
                .select(cb.construct(Temperature.class,
                        location.get(Location_.CITY), from.get(TemperatureEvent_.TEMPERATURE),
                        from.get(TemperatureEvent_.DATE), location.get(Location_.LATITUDE),
                        location.get(Location_.LONGITUDE)))
                .where(cityCriteria, dateCriteria)
                .orderBy(cb.desc(from.get(TemperatureEvent_.DATE)));
        Temperature temp = currentSession().createQuery(criteriaQuery).getResultStream()
                .findFirst()
                .orElseThrow(exceptionSupplier);

        // Get min and max temp
        CriteriaQuery<Tuple> getMinMaxTempQuery = cb.createTupleQuery();
        Root<TemperatureEvent> fromMinMax = getMinMaxTempQuery.from(TemperatureEvent.class);
        Join locationForMax = fromMinMax.join(TemperatureEvent_.LOCATION);
        Predicate locationCriteriaForMinMax = getLocationCriteria(latitude, longitude, locationForMax, cb);
        Predicate dateBetween = cb.between(fromMinMax.get(TemperatureEvent_.DATE),
                Datehelper.getDateWithoutTime(date),
                Datehelper.getTomorrowDate(date));
        getMinMaxTempQuery.select(cb.tuple(cb.max(fromMinMax.get(TemperatureEvent_.TEMPERATURE)),
                cb.min(fromMinMax.get(TemperatureEvent_.TEMPERATURE))))
                .where(locationCriteriaForMinMax, dateBetween);
        Tuple temperature = currentSession().createQuery(getMinMaxTempQuery).uniqueResultOptional().orElseThrow(exceptionSupplier);
        temp.setMax(temperature.get(0, Float.class));
        temp.setMin(temperature.get(1, Float.class));
        return temp;
    }

    public HourlyTemperature findByCityHourly(Double latitude, Double longitude, Date date,
                                              Supplier<? extends WebApplicationException> exceptionSupplier) {
        final CriteriaBuilder cb = currentSession().getCriteriaBuilder();

        CriteriaQuery<Tuple> getHourlyQuery = cb.createTupleQuery();
        Root<TemperatureEvent> from = getHourlyQuery.from(TemperatureEvent.class);
        Join location = from.join(TemperatureEvent_.LOCATION);
        Predicate locationCriteria = getLocationCriteria(latitude, longitude, location, cb);
        Predicate dateBetween = cb.between(from.get(TemperatureEvent_.DATE),
                Datehelper.getDateWithoutTime(date),
                Datehelper.getTomorrowDate(date));
        getHourlyQuery.select(cb.tuple(from.get(TemperatureEvent_.HOUR),
                cb.avg(from.get(TemperatureEvent_.TEMPERATURE))))
                .where(locationCriteria, dateBetween)
                .orderBy(cb.asc(from.get(TemperatureEvent_.HOUR)))
                .groupBy(from.get(TemperatureEvent_.HOUR));
        List<Tuple> temperature = currentSession().createQuery(getHourlyQuery).getResultList();
        if (temperature.isEmpty())
            throw exceptionSupplier.get();
        HashMap<String, Double> hourList = new LinkedHashMap<>();
        temperature.forEach(hour -> hourList.put(hour.get(0, Integer.class).toString().concat(":00"), hour.get(1, Double.class)));
        return HourlyTemperature.builder()
                .city(getCity(latitude, longitude, cb).orElseThrow(exceptionSupplier))
                .latitude(latitude)
                .longitude(longitude)
                .hourlyTemperatures(hourList)
                .build();
    }


    public List<GlobalTemperature> findGlobalTemperature(Date fromDate, Date toDate, int limit, String type,
                                                         Supplier<? extends WebApplicationException> exceptionSupplier) {
        int max = limit > 10 || limit < 1 ? 10 : limit;

        final CriteriaBuilder cb = currentSession().getCriteriaBuilder();

        CriteriaQuery<Tuple> getGlobalTemp = cb.createTupleQuery();
        Root<TemperatureEvent> from = getGlobalTemp.from(TemperatureEvent.class);
        Join location = from.join(TemperatureEvent_.LOCATION);
        Predicate dateBetween = cb.between(from.get(TemperatureEvent_.DATE),
                fromDate,
                toDate);
        Expression tempType;
        if (type.equalsIgnoreCase("max")) {
            tempType = cb.max(from.get(TemperatureEvent_.TEMPERATURE));
            getGlobalTemp.select(cb.tuple(location.get(Location_.ID).alias("location"),
                    tempType.alias("foundTemp")))
                    .where(dateBetween)
                    .orderBy(cb.desc(tempType))
                    .groupBy(location.get(Location_.ID));
        }
        else {
            tempType = cb.min(from.get(TemperatureEvent_.TEMPERATURE));
            getGlobalTemp.select(cb.tuple(location.get(Location_.ID).alias("location"),
                    tempType.alias("foundTemp")))
                    .where(dateBetween)
                    .orderBy(cb.asc(tempType))
                    .groupBy(location.get(Location_.ID));
        }

        List<Tuple> maxTempsByLocation = currentSession().createQuery(getGlobalTemp).setMaxResults(max).getResultList();
        if (maxTempsByLocation.isEmpty())
            throw exceptionSupplier.get();

        CriteriaQuery<Location> getList =  cb.createQuery(Location.class);
        Root<Location> fromTable = getList.from(Location.class);
        CriteriaBuilder.In<Long> in = cb.in(fromTable.get(Location_.ID));
        maxTempsByLocation.forEach(x -> in.value(x.get("location", Long.class)));
        getList.select(fromTable).where(in);
        List<Location> locations = currentSession().createQuery(getList).getResultList();

        return maxTempsByLocation.stream().map(tuple -> {
            Long locationId = tuple.get("location", Long.class);
            Location eachLocation = locations.stream().filter(loc -> loc.getId() == locationId).findFirst().orElseThrow(exceptionSupplier);
            return GlobalTemperature.builder()
                    .temperature(tuple.get("foundTemp", Float.class))
                    .latitude(eachLocation.getLatitude())
                    .longitude(eachLocation.getLongitude())
                    .city(eachLocation.getCity())
                    .build();
        }).collect(Collectors.toList());
    }


    public long create(TemperatureEvent event) {
        return persist(event).getId();
    }

    private Predicate getLocationCriteria(final Double latitude, final Double longitude, Join join, CriteriaBuilder cb) {
        return cb.and(
                cb.equal(join.get(Location_.LATITUDE), latitude),
                cb.equal(join.get(Location_.LONGITUDE), longitude));
    }

    private Optional<String> getCity(final Double latitude, final Double longitude, final CriteriaBuilder cb) {
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<Location> from = criteriaQuery.from(Location.class);
        Predicate latAndLong = cb.and(
                cb.equal(from.get(Location_.LATITUDE), latitude),
                cb.equal(from.get(Location_.LONGITUDE), longitude));
        criteriaQuery.select(from.get(Location_.CITY))
                .where(latAndLong);
        return Optional.of(currentSession().createQuery(criteriaQuery).getSingleResult());
    }
}
