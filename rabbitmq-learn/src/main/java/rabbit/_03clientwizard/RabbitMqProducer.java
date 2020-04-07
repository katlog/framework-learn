package rabbit._03clientwizard;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;
import rabbit.BaseRabbit;

import java.io.IOException;

public class RabbitMqProducer extends BaseRabbit {


    /**  消息持久化 */
    @Test
    public void publish_messageDurable1() throws IOException {
        publishDurable("exchange_messagedurable_demo", "routing_durable_key", "durable message 1 ");
    }

    @Test
    public void publish_messageDurable2() throws IOException {
        defaultChannel.basicPublish("exchange_messagedurable_demo","routing_durable_key"
                , MessageProperties.PERSISTENT_TEXT_PLAIN
                ,"durable message 2 ".getBytes());
    }


    /**  过期消息 */
    @Test
    public void publish_expireMessage() throws IOException {
        defaultChannel.basicPublish("exchange_exipremessage_demo","routing_messageexipire_key"
                ,new AMQP.BasicProperties().builder().expiration("60000").build()
                ,"expire message 2 ".getBytes());
    }
}
