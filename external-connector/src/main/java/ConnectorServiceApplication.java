import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.weather.service.external.connector.service.ConnectorResource;
import com.weather.service.external.connector.service.ConnectorService;
import com.weather.service.external.connector.client.AMQPClientImpl;
import com.weather.service.external.connector.client.api.AMQPClient;
import com.weather.service.external.connector.config.ConnectorConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConnectorServiceApplication extends Application<ConnectorConfiguration> {
    public static void main(String[] args) throws Exception {
        final String command = args.length > 0 ? args[0] : "server";
        final String applicationYamlFile = args.length > 1 ? args[1] : "external-connector/application.yml";
        new ConnectorServiceApplication().run(command, applicationYamlFile);
    }

    @Override
    public String getName() {
        return "external-connector-service";
    }

    @Override
    public void initialize(Bootstrap<ConnectorConfiguration> bootstrap) {
        // nothing to initialize
    }

    @Override
    public void run(ConnectorConfiguration configuration,
                    Environment environment) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = createConnectionFactory(configuration);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(configuration.getAmqpConfiguration().getExchange(), BuiltinExchangeType.DIRECT);
        channel.queueDeclare(configuration.getAmqpConfiguration().getQueue(), false, false, false, null);
        channel.queueBind(
                configuration.getAmqpConfiguration().getQueue(),
                configuration.getAmqpConfiguration().getExchange(),
                configuration.getAmqpConfiguration().getRoutingKey());

        AMQPClient amqpClient = new AMQPClientImpl(
                channel,
                connection,
                configuration.getAmqpConfiguration().getExchange(),
                configuration.getAmqpConfiguration().getRoutingKey());

        ConnectorService connectorService = new ConnectorService(amqpClient);
        environment.jersey().register(new ConnectorResource(connectorService));

    }

    private ConnectionFactory createConnectionFactory(ConnectorConfiguration configuration) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setConnectionTimeout(1000);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost(configuration.getAmqpConfiguration().getHost());
        factory.setPort(configuration.getAmqpConfiguration().getPort());
        return factory;
    }

}
