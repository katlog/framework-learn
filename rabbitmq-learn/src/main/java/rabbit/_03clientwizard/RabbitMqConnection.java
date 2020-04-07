package rabbit._03clientwizard;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;
import rabbit.BaseRabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqConnection extends BaseRabbit {


    @Test
    public void channel_() throws IOException, TimeoutException {
        Connection connection = FACTORY.newConnection();

        Channel channel = connection.createChannel();

        System.out.println("channel = " + channel);
        System.out.println("channel.getClass() = " + channel.getClass());

        channel.close();
        connection.close();
    }





}
