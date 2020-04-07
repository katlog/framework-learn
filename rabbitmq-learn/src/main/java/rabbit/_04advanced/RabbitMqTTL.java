package rabbit._04advanced;

import com.rabbitmq.client.AMQP;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**  TTL */
public class RabbitMqTTL extends BaseRabbit {

    /**  在队列上设置ttl */
    @Test
    public void publish_queueTTL() throws IOException {

        String queueName = "publish_queueTTL_queue";
        boolean durable = true;
        boolean exclusive = false;
        boolean autoDelete = false;

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl",6000);
        defaultChannel.queueDeclare(queueName, durable, exclusive, autoDelete, args);

    }

    /**  在消息属性上设置 */
    @Test
    public void publish_amqpTTL() throws IOException, InterruptedException {

        Meta meta = quickDeclareExchangeQueue("publish_amqpTTL", true, false);
        String exchangeName = meta.getExchange();
        String routingKey = meta.getRoutingKey();

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        //持久化消息
        builder.deliveryMode(2);
        //设置TTL=60000ms
        builder.expiration("60000");
        AMQP.BasicProperties properties = builder.build();
        defaultChannel.basicPublish(exchangeName, routingKey, true, properties, "publish_amqpTTL message".getBytes());

        Thread.sleep(60000);
    }
}
