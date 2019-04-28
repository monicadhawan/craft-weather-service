import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.weather.service.ingester.client.AMQPClientImpl;
import com.weather.service.ingester.client.api.AMQPClient;
import com.weather.service.ingester.config.IngesterConfiguration;
import com.weather.service.query.data.Location;
import com.weather.service.query.data.TemperatureEvent;
import com.weather.service.query.data.dao.TemperatureDAO;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
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

    private final HibernateBundle<IngesterConfiguration> hibernate = new HibernateBundle<IngesterConfiguration>(TemperatureEvent.class,
            Location.class) {
        public DataSourceFactory getDataSourceFactory(IngesterConfiguration configuration) {
            return configuration.getDatabase();
        }
    };


    @Override
    public String getName() {
        return "ingester-service";
    }

    @Override
    public void initialize(Bootstrap<IngesterConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(IngesterConfiguration configuration,
                    Environment environment) throws IOException, TimeoutException {
        final TemperatureDAO temperatureDAO = new TemperatureDAO(hibernate.getSessionFactory());
        ConnectionFactory connectionFactory = createConnectionFactory(configuration);
        Connection connection = connectionFactory.newConnection();
        AMQPClient amqpClient = new AMQPClientImpl(connection.createChannel(), configuration.getAmqpConfiguration().getQueue());
        QueueConsumer queueConsumer = new UnitOfWorkAwareProxyFactory(hibernate)
                .create(QueueConsumer.class,
                        new Class[] {AMQPClient.class, TemperatureDAO.class} ,
                        new Object[] {amqpClient, temperatureDAO});
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
