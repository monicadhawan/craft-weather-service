package com.weather.service;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

@Entity
@Table(name = "TEMPERATURE_EVENTS")
public class TemperatureDAO extends AbstractDAO<TemperatureEvent> {

    public TemperatureDAO(SessionFactory factory) {
        super(factory);
    }

/*    public TemperatureEvent findByCity(Double latitude, Double longitude, Date date) {
        final CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<TemperatureEvent> criteriaQuery = cb.createQuery(TemperatureEvent.class);
        Root<TemperatureEvent> from = criteriaQuery.from(TemperatureEvent.class);
        Predicate dateCriteria = cb.lessThanOrEqualTo(from.<Date>get("date"), date);

        Join location = from.join(TemperatureEvent_.LOCATION);
        Predicate cityCriteria = cb.and(
                cb.equal(location.get(Location_.LATITUDE), latitude),
                cb.equal(location.get(Location_.LONGITUDE), longitude));

        criteriaQuery
                .select(from)
                .where(cityCriteria, dateCriteria)
                .orderBy(cb.desc(from.get(TemperatureEvent_.DATE)));
        return currentSession().createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
    }*/

    public Temperature findByCityMinMax(Double latitude, Double longitude, Date date) {
        final CriteriaBuilder cb = currentSession().getCriteriaBuilder();

        // Get current temperature
        CriteriaQuery<Temperature> criteriaQuery = cb.createQuery(Temperature.class);
        Root<TemperatureEvent> from = criteriaQuery.from(TemperatureEvent.class);
        Predicate dateCriteria = cb.lessThanOrEqualTo(from.<Date>get(TemperatureEvent_.DATE), date);
        Join location = from.join(TemperatureEvent_.LOCATION);
        Predicate cityCriteria = getLocationCriteria(latitude, longitude, location, cb);
        criteriaQuery
                .select(cb.construct(Temperature.class, from.get(TemperatureEvent_.ID),
                        location.get(Location_.CITY), from.get(TemperatureEvent_.TEMPERATURE),
                        from.get(TemperatureEvent_.DATE), location.get(Location_.LATITUDE),
                        location.get(Location_.LONGITUDE)))
                .where(cityCriteria, dateCriteria)
                .orderBy(cb.desc(from.get(TemperatureEvent_.DATE)));
        Temperature temp =  currentSession().createQuery(criteriaQuery).setMaxResults(1).getSingleResult();

        // Get Max temp
        //TODO date comparison
        CriteriaQuery<Float> getMaxTempQuery = cb.createQuery(Float.class);
        Root<TemperatureEvent> fromForMax = getMaxTempQuery.from(TemperatureEvent.class);
        Join locationForMax = fromForMax.join(TemperatureEvent_.LOCATION);
        Predicate cityCriteriaForMax = getLocationCriteria(latitude, longitude, locationForMax, cb);
        getMaxTempQuery.select(cb.max(fromForMax.<Float>get(TemperatureEvent_.TEMPERATURE)))
                .where(cityCriteriaForMax);
        float max = currentSession().createQuery(getMaxTempQuery).getSingleResult();
        temp.setMax(max);

        // Get Min temp
        //TODO date comparison
        CriteriaQuery<Float> getMinTempQuery = cb.createQuery(Float.class);
        Root<TemperatureEvent> fromForMin = getMinTempQuery.from(TemperatureEvent.class);
        Join locationForMin = fromForMin.join(TemperatureEvent_.LOCATION);
        Predicate cityCriteriaForMin = getLocationCriteria(latitude, longitude, locationForMin, cb);
        getMinTempQuery.select(cb.min(fromForMin.<Float>get(TemperatureEvent_.TEMPERATURE)))
                .where(cityCriteriaForMin);
        float min = currentSession().createQuery(getMinTempQuery).getSingleResult();
        temp.setMin(min);

        return temp;
    }

    public long create(TemperatureEvent event) {
        return persist(event).getId();
    }

    private Predicate getLocationCriteria(final Double latitude, final Double longitude, Join join, CriteriaBuilder cb) {
        return cb.and(
                cb.equal(join.get(Location_.LATITUDE), latitude),
                cb.equal(join.get(Location_.LONGITUDE), longitude));
    }
}
