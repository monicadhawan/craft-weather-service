import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.weather.service.ingester.client.AMQPClient;
import com.weather.service.ingester.client.WeatherRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.Base64;

@AllArgsConstructor
public class QueueConsumer implements Runnable, Consumer {

    private AMQPClient amqpClient;

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

    public void handleDelivery(String consumerTag, Envelope env,
                               AMQP.BasicProperties props, byte[] body) throws IOException {
       WeatherRequest dataItem = new ObjectMapper().readValue(SerializationUtils.deserialize(body).toString(), WeatherRequest.class);
        System.out.println("Handle delivery" + dataItem.toString());
    }

    public void handleCancel(String consumerTag) {}
    public void handleCancelOk(String consumerTag) {}
    public void handleRecoverOk(String consumerTag) {}
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {}
}