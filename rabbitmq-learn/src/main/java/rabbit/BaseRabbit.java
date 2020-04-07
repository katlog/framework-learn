package rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BaseRabbit {

    protected static final ConnectionFactory FACTORY = new ConnectionFactory();
    protected Connection defaultConnection;
    protected Channel defaultChannel;

    @BeforeClass
    public static void  beforeClass(){
        initFactory(FACTORY);
    }

    protected static void initFactory( ConnectionFactory factory ) {
        factory.setPassword(ConfigConstants.PASSWORD);
        factory.setUsername(ConfigConstants.USER_NAME);

        factory.setHost(ConfigConstants.IP_ADDRESS);
        factory.setPort(ConfigConstants.PORT);
    }


    @Before
    public void before() throws IOException, TimeoutException {
        defaultConnection = FACTORY.newConnection();
        defaultChannel = defaultConnection.createChannel();
    }

    @After
    public void after() throws IOException, TimeoutException {
        defaultChannel.close();
        defaultConnection.close();
    }

    protected void publishDurable(String exchangeName,String routingKey,String message) throws IOException {
        defaultChannel.basicPublish(exchangeName,routingKey
                ,new AMQP.BasicProperties()
                        .builder()
                        .contentType("text/plain")
                        .deliveryMode(2)
                        .build()
                ,message.getBytes());
    }
}
