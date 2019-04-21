package com.weather.service;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.WebApplicationException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Entity
@Table(name = "TEMPERATURE_EVENTS")
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
                .groupBy(from.get(TemperatureEvent_.HOUR));
        List<Tuple> temperature = currentSession().createQuery(getHourlyQuery).getResultList();
        if (temperature.isEmpty())
            throw exceptionSupplier.get();
        HashMap<String, Double> hourList = new HashMap<>();
        temperature.sort(Comparator.comparing(x -> x.get(0, Integer.class)));
        temperature.forEach(hour -> hourList.put(hour.get(0, Integer.class).toString().concat(":00"), hour.get(1, Double.class)));
        return HourlyTemperature.builder()
                .city(getCity(latitude, longitude, cb).orElseThrow(exceptionSupplier))
                .latitude(latitude)
                .longitude(longitude)
                .hourlyTemperatures(hourList)
                .build();
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
