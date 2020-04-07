package rabbit._03clientwizard;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.junit.Assert;
import org.junit.Test;
import rabbit.BaseRabbit;

import java.io.IOException;
import java.util.UUID;

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

    /**  找不到交换器时: channel再次尝试连接时 channel会处于关闭的状态*/
    @Test
    public void publish_fail_channelClose() throws IOException {

        System.out.println("send first");
        // No.	Time	Source	Destination	Protocol	Length	Info
        // 会返回类似	Channel.Close reply=NOT_FOUND - no exchange '8647f23d-b21f-4953-8563-fb36bd75adf2' in vhost '/'
        publishDurable(UUID.randomUUID().toString(),"","never find exchange ...");

        Assert.assertTrue(defaultChannel.isOpen());

        System.out.println("send send");
        try {
            // 再次发送时会 Channel.Close-Ok （这个时候channel才处于关闭状态）
            publishDurable(UUID.randomUUID().toString(),"","never find exchange ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(defaultChannel.isOpen());
    }
}

