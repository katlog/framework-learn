package rabbit._03clientwizard;

import com.rabbitmq.client.*;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RabbitMqConsumer extends BaseRabbit {


    /**  推模式消费：会一下子拿到全部消息 */
    @Test
    public void consume_basicPush() throws IOException, InterruptedException {
        boolean autoAck = false;
        // basicQos方法允许限制【信道】上的消费者所能保持的最大未确认消息的数量
        defaultChannel.basicQos(64);
        defaultChannel.basicConsume("message_durable_queue", autoAck, "myConsumerTag"
                , new DefaultConsumer(defaultChannel){
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        long deliveryTag = print(consumerTag, envelope, properties, body);
                        //(process the message components here ..)
                        defaultChannel.basicAck(deliveryTag ,false);
                    }
                });

        Thread.sleep(500);
    }

    /**  推模式消费：有多个消费者时用consumerTag来区分彼此 */
    @Test
    public void consume_push_multiConsume() throws IOException, InterruptedException, TimeoutException {

        String exchangeName = "exchange_multiconsume_demo";
        String routingKey = "routing_mullticonsume_key";

        defaultChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true, false, null);

        defaultChannel.basicQos(64);
        defaultChannel.queueDeclare("multi_consume_queue1", true, false, true, null);
        defaultChannel.queueBind("multi_consume_queue1", exchangeName, routingKey);

        defaultChannel.basicQos(64);
        defaultChannel.queueDeclare("multi_consume_queue2", true, false, true, null);
        defaultChannel.queueBind("multi_consume_queue2", exchangeName, routingKey);

        publishDurable(exchangeName,routingKey, "multi consume message 1");
        publishDurable(exchangeName,routingKey, "multi consume message 2");

        System.out.println("myConsumerTag1 start consume........ " );
        defaultChannel.basicConsume("multi_consume_queue1", false, "myConsumerTag1"
                , new DefaultConsumer(defaultChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        long deliveryTag = print(consumerTag, envelope, properties, body);
                        defaultChannel.basicAck(deliveryTag, false);
                    }
                });


        System.out.println("myConsumerTag2 start consume........ " );
        //consumerTag不能相同
        defaultChannel.basicConsume("multi_consume_queue2", false, "myConsumerTag2"
                , new DefaultConsumer(defaultChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        long deliveryTag = print(consumerTag, envelope, properties, body);
                        defaultChannel.basicAck(deliveryTag, false);
                    }
                });

        Thread.sleep(50000);
    }


    /**  拉模式 */
    @Test
    public void consume_get() throws IOException {

        String exchange = "exchange_getmode_demo";
        defaultChannel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, false, true, null);

        String queue = "getmode_consume_queue";
        defaultChannel.queueDeclare(queue, false, false, true, null);

        String routingKey = "routing_getmode_key";
        defaultChannel.queueBind(queue, exchange, routingKey, null);

        publishDurable(exchange, routingKey, "get mode consume message1");
        publishDurable(exchange, routingKey, "get mode consume message2");
        publishDurable(exchange, routingKey, "get mode consume message3");

        // 每个ack都单独确认了一次
        GetResponse response = consumeMessageByGetMode(queue);
        defaultChannel.basicAck(response.getEnvelope().getDeliveryTag(), false);

         response = consumeMessageByGetMode(queue);
        defaultChannel.basicAck(response.getEnvelope().getDeliveryTag(), false);

        response = consumeMessageByGetMode(queue);
        defaultChannel.basicAck(response.getEnvelope().getDeliveryTag(), false);

    }

    /**  拉模式 */
    @Test
    public void consume_batchGet() throws IOException {

        String exchange = "exchange_batchGetMode_demo";
        defaultChannel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, false, true, null);

        String queue = "batchGetMode_consume_queue";
        defaultChannel.queueDeclare(queue, false, false, true, null);

        String routingKey = "routing_batchGetMode_key";
        defaultChannel.queueBind(queue, exchange, routingKey, null);

        publishDurable(exchange, routingKey, "get mode consume message1");
        publishDurable(exchange, routingKey, "get mode consume message2");
        publishDurable(exchange, routingKey, "get mode consume message3");

        GetResponse response = consumeMessageByGetMode(queue);
        System.out.println("response.getEnvelope().getDeliveryTag() = " + response.getEnvelope().getDeliveryTag());
        GetResponse response1 = consumeMessageByGetMode(queue);
        System.out.println("response1.getEnvelope().getDeliveryTag() = " + response1.getEnvelope().getDeliveryTag());
        GetResponse response2 = consumeMessageByGetMode(queue);
        System.out.println("response2.getEnvelope().getDeliveryTag() = " + response2.getEnvelope().getDeliveryTag());

        // 需要用最后一个deliveryTag才行，使用response只会ack确认1个，使用response1只会ack确认2个
        defaultChannel.basicAck(response1.getEnvelope().getDeliveryTag(), true);

    }

    private GetResponse consumeMessageByGetMode(String queue) throws IOException {

        GetResponse response = defaultChannel.basicGet(queue, false);
        System.out.println(" 消费........................................."  );
        System.out.println("MessageCount() = " + response.getMessageCount());
        System.out.println(new String(response.getBody()));

        return response;
    }

    /**  消费中Consumer的处理过程 */
    @Test
    public void consume_consumer_process() throws IOException, InterruptedException {
        Meta meta = quickDeclareExchangeQueue("consume_consumer_process");
        publishDurable(meta, "consume_consumer_process message 1...");



        defaultChannel.basicConsume(meta.getQueue(), new Consumer() {
            @Override
            public void handleConsumeOk(String consumerTag) {
                // 会在其他方法之前调用，返回消费者标签
                System.out.println("handleConsumeOk consumerTag = " + consumerTag);
            }

            @Override
            public void handleCancelOk(String consumerTag) {
                // 显式地或者隐式地取消订阅时调用(通过channel.basicCancel方法来显式地取消一个消费者的订阅)
                System.out.println("handleCancelOk consumerTag = " + consumerTag);
            }

            @Override
            public void handleCancel(String consumerTag) throws IOException {
                // 显式地或者隐式地取消订阅时调用(通过channel.basicCancel方法来显式地取消一个消费者的订阅)
                System.out.println("handleCancel consumerTag = " + consumerTag);
            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                // 当Channel或者Connection关闭的时候会调用
                System.out.println("handleShutdownSignal consumerTag = " + consumerTag);
            }

            @Override
            public void handleRecoverOk(String consumerTag) {
                System.out.println("handleRecoverOk consumerTag = " + consumerTag);
            }

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("handleDelivery consumerTag = " + consumerTag);
                defaultChannel.basicCancel(consumerTag);
            }
        });

        Thread.sleep(60000);
    }

}
