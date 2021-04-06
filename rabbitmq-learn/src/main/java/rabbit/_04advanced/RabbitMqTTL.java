package rabbit._04advanced;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**  TTL */
public class RabbitMqTTL extends BaseRabbit {

    /**  在队列上设置ttl：消息过期后会立马从队列中删去 */
    @Test
    public void publish_queueTTL() throws IOException {

        String queue = "publish_queueTTL_queue";
        String exchange = "publish_queueTTL_exchange";
        String routingKey = "routingKey";

        defaultChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl",6000);
        defaultChannel.queueDeclare(queue, true, false, false, args);
        defaultChannel.queueBind(queue, exchange, routingKey);

        defaultChannel.basicPublish(exchange, routingKey
                , MessageProperties.PERSISTENT_TEXT_PLAIN, "publish ttl message by queue ttl ...".getBytes());

    }

    /**  在消息属性上设置：感觉和上面的差不多诶，也是过了6秒就不见了 */
    @Test
    public void publish_amqpTTL() throws IOException, InterruptedException {

        Meta meta = quickDeclareExchangeQueue("publish_amqpTTL", true, false);
        String exchangeName = meta.getExchange();
        String routingKey = meta.getRoutingKey();

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        //持久化消息
        builder.deliveryMode(2);
        //设置TTL=60000ms
        builder.expiration("6000");
        AMQP.BasicProperties properties = builder.build();
        defaultChannel.basicPublish(exchangeName, routingKey, true, properties, "publish_amqpTTL message".getBytes());

        Thread.sleep(6000);
    }

    /** 设置队列的ttl：可控制队列被自动删除前处于未使用状态的时间。
     *          未使用的意思是队列上没任何的消费者，
     *          队列也没被重新声明，
     *          且在过期时间段内也未调用过Basic.Get命令。 */
    @Test
    public void publish_queueDeclareTtl() throws IOException, InterruptedException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-expires", 10000);
        System.out.println("declare a expire queue  ");
        defaultChannel.queueDeclare("my_expire_queue", true, false, false, args);

        Thread.sleep(10000L);
        System.out.println("args = ");
    }
}
