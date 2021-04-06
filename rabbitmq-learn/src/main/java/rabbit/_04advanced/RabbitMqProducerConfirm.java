package rabbit._04advanced;

import com.rabbitmq.client.*;
import org.junit.Before;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**  生产者确认，两种：事务；发送方确认 */
public class RabbitMqProducerConfirm extends BaseRabbit {


    public static final int BATCH_COUNT = 10;

    private Channel confirmChannel ;
    private SortedSet<Long> confirmSet;
    @Before
    public void connection() throws IOException {
        confirmChannel = defaultConnection.createChannel();
        confirmSet = new TreeSet<>();
    }


    /**  publisher confirm机制：采用串行等待 */
    @Test
    public void publish_basicConfirm() throws IOException {

        String queue = "publish_confirm_basic_queue";
        String exchange = "publish_confirm_basic_exchange";
        String routingKey = "routingKey";


        confirmChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        confirmChannel.queueDeclare(queue, true, false, false, null);
        confirmChannel.queueBind(queue, exchange, routingKey);


        try {
            // 将信道设置为publish confirm模式
            confirmChannel.confirmSelect();

            confirmChannel.basicPublish(exchange, routingKey
                    , MessageProperties.PERSISTENT_TEXT_PLAIN, "publish confirm message by confirm queue ...".getBytes());

            if (!confirmChannel.waitForConfirms()) {
                System.out.println("send msg failed");
                // do something else
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void publish_batchConfirm() throws IOException {

        String queue = "publish_confirm_batch_queue";
        String exchange = "publish_confirm_batch_exchange";
        String routingKey = "routingKey";

        confirmChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        confirmChannel.queueDeclare(queue, true, false, false, null);
        confirmChannel.queueBind(queue, exchange, routingKey);

        try{
            confirmChannel.confirmSelect();
            int MsgCount = 0;
            while(true){
                confirmChannel.basicPublish("exchange", "routingKey", null, "batch confirm test".getBytes());
                // 将发送出去的消息存入缓存中，缓存可以是ArrayList或BlockingQueue中
                if(++MsgCount >= BATCH_COUNT){
                    MsgCount = 0;
                    try{
                        if(confirmChannel.waitForConfirms()) {
                            // 将缓存中的消息清空
                            continue;
                        }
                        // 将缓存中的消息重新发送
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                        // 将缓存中的消息重新发送
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void publish_asyncConfirm() throws IOException {

        String queue = "publish_confirm_async_queue";
        String exchange = "publish_confirm_async_exchange";
        String routingKey = "routingKey";

        confirmChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        confirmChannel.queueDeclare(queue, true, false, false, null);
        confirmChannel.queueBind(queue, exchange, routingKey);


        confirmChannel.confirmSelect();

        confirmChannel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Nack,SeqNo:" + deliveryTag + ",multiple:" + multiple);
                if (multiple) {
                    confirmSet.headSet(deliveryTag - 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    confirmSet.headSet(deliveryTag - 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
                // 注意这里要添加处理消息重发的场景
            }
        });
        // 下面演示一直发送消息的场景
        while (true) {
            long nextSeqNo = confirmChannel.getNextPublishSeqNo();
            confirmChannel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN
                    ,"msg confirm async..".getBytes());
            confirmSet.add(nextSeqNo);
        }

    }

}
