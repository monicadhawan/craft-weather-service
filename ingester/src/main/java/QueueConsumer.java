import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.weather.service.ingester.client.AMQPClient;
import com.weather.service.ingester.client.WeatherRequest;
import com.weather.service.query.data.Location;
import com.weather.service.query.data.TemperatureEvent;
import com.weather.service.query.data.TemperatureEvent_;
import com.weather.service.query.data.dao.TemperatureDAO;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;

@AllArgsConstructor
public class QueueConsumer implements Runnable, Consumer {

    private AMQPClient amqpClient;
    private TemperatureDAO temperatureDAO;

    public void run() {
        try {
            String data = amqpClient.readMessage(this);
            System.out.println(data + " : Worked");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleConsumeOk(String consumerTag) {
        System.out.println("Consumer "+ consumerTag +" registered");
    }

    @UnitOfWork
    public void handleDelivery(String consumerTag, Envelope env,
                               AMQP.BasicProperties props, byte[] body) throws IOException {
       WeatherRequest dataItem = new ObjectMapper().readValue(SerializationUtils.deserialize(body).toString(), WeatherRequest.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getDefault());
        System.out.println("Data received : " + dataItem.toString());
        TemperatureEvent event = null;
        try {
            event = TemperatureEvent.builder()
                    .date(formatter.parse(dataItem.getDate()))
                    .hour(dataItem.getHour())
                    .location(Location.builder()
                            .city("Mountain View")
                            .latitude(dataItem.getLat())
                            .longitude(dataItem.getLongitude())
                            .build())
                    .temperature(dataItem.getTemperature())
                    .wind(dataItem.getWind())
                    .build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        temperatureDAO.create(event);

    }

    public void handleCancel(String consumerTag) {}
    public void handleCancelOk(String consumerTag) {}
    public void handleRecoverOk(String consumerTag) {}
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {}
}