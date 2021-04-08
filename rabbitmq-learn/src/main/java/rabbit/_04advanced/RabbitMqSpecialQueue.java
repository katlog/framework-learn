package rabbit._04advanced;

import com.rabbitmq.client.*;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**  测试特殊队列 */
public class RabbitMqSpecialQueue extends BaseRabbit {

    /** 死信队列： */
    @Test
    public void dlxQueue_1() throws IOException {

        String dlxExchange = "dlx_exchange_demo1_dlx";




        //  对应的死信队列
        String dlxQueue = "dlx_queue_demo1_dlx";
        defaultChannel.exchangeDeclare(dlxExchange, BuiltinExchangeType.FANOUT, true, false, null);
        defaultChannel.queueDeclare(dlxQueue, true, false, false, null);
        defaultChannel.queueBind(dlxQueue, dlxExchange, "", null);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", dlxExchange);
        arguments.put("x-message-ttl", 10000);
        String exchange = "dlx_exchange_demo1";
        String queue = "dlx_queue_demo1";
        defaultChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        defaultChannel.queueDeclare(queue, true, false, false, arguments);
        defaultChannel.queueBind(queue, exchange, "routingKey", null);


        /**
         *   以下三种情况：（vs 备份队列-路由不到时）
         *  消息被拒绝（Basic.Reject/Basic.Nack），且设置requeue参数为false；
         *  消息过期；
         *  队列达到最大长度。
         * */
        defaultChannel.basicPublish(exchange, "routingKey", MessageProperties.PERSISTENT_TEXT_PLAIN
                , "dlx queue 1 demo message".getBytes());


    }

    /** 延迟队列 */
    @Test
    public void delayQueue() throws IOException, InterruptedException {

        // dlx
        Meta meta = quickDeclareExchangeQueue("dlx_delay_queue");


        // 20 s 的延迟队列
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 20000);
        arguments.put("x-dead-letter-exchange", meta.getExchange());
        defaultChannel.exchangeDeclare("dlx_delay_exchange_demo1", BuiltinExchangeType.DIRECT,true,false, arguments);
        defaultChannel.queueDeclare("dlx_delay_queue_demo1", true, false, false, null);
        defaultChannel.queueBind("dlx_delay_queue_demo1", "dlx_delay_exchange_demo1", "routingKey", null);

        defaultChannel.basicPublish("dlx_delay_exchange_demo1", "routingKey", MessageProperties.PERSISTENT_TEXT_PLAIN
                , "dlx delay queue 1 demo message".getBytes());

        defaultChannel.basicConsume(meta.getQueue(), false, "myConsumerTag"
                , new DefaultConsumer(defaultChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        long deliveryTag = print(consumerTag, envelope, properties, body);
                        defaultChannel.basicAck(deliveryTag, false);
                    }
                });

        Thread.sleep(21000L);

    }

    /** 优先级队列 */
    @Test
    public void priorityQueue(){

    }

}
