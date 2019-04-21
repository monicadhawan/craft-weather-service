import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.weather.service.ingester.client.AMQPClient;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;

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
                               AMQP.BasicProperties props, byte[] body) {
        String dataItem = SerializationUtils.deserialize(body);
        System.out.println("Handle delivery" + dataItem);
    }

    public void handleCancel(String consumerTag) {}
    public void handleCancelOk(String consumerTag) {}
    public void handleRecoverOk(String consumerTag) {}
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {}
}