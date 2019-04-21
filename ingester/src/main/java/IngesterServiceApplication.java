import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.weather.service.ingester.client.AMQPClient;
import com.weather.service.ingester.config.IngesterConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class IngesterServiceApplication extends Application<IngesterConfiguration> {
    public static void main(String[] args) throws Exception {
        final String command = args.length > 0 ? args[0] : "server";
        final String applicationYamlFile = args.length > 1 ? args[1] : "ingester/application.yml";
        new IngesterServiceApplication().run(command, applicationYamlFile);
    }


    @Override
    public String getName() {
        return "ingester-service";
    }

    @Override
    public void initialize(Bootstrap<IngesterConfiguration> bootstrap) {
        // nothing to initialize
    }

    @Override
    public void run(IngesterConfiguration configuration,
                    Environment environment) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = createConnectionFactory(configuration);
        Connection connection = connectionFactory.newConnection();
        AMQPClient amqpClient = new AMQPClient(connection.createChannel(), connection, configuration.getAmqpConfiguration().getQueue());

        QueueConsumer queueConsumer = new QueueConsumer(amqpClient);
        Thread consumerThread = new Thread(queueConsumer);
        consumerThread.start();

    }

    private ConnectionFactory createConnectionFactory(IngesterConfiguration configuration) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setConnectionTimeout(1000);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost(configuration.getAmqpConfiguration().getHost());
        factory.setPort(configuration.getAmqpConfiguration().getPort());
        return factory;
    }

}
