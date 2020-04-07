package rabbit;

import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitProducerTest {

    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY = "routingkey_demo";
    private static final String QUEUE_NAME = "queue_demo";
    private static final String IP_ADDRESS = "localhost";
    private static final int PORT = 5672;


    public void send(Consumer<Channel> conFun) {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);
        factory.setUsername("root");
        factory.setPassword("root123");

        Connection connection = null;
         Channel channel = null;
        try {
            // 创建连接
            connection = factory.newConnection();
            // 创建通信
            channel = connection.createChannel();

            // 创建一个type=direct、持久化的、非自动删除的交换器
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
            // 创建一个持久化、非排他的、非自动删除的队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            // 将交换器与队列通过路由键绑定
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            conFun.accept(channel);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            // 注意顺序
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void produce() {
        send(channel -> {
            // 发送一条持久化的消息：hello world
            String message = "Hello World";
            try {
            channel.basicPublish(EXCHANGE_NAME,"", true,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    "mandatory test".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            };
        });
    }


    @Test
    public void produce1() {
        send(channel -> {
            try {
                // 发送一条持久化的消息：hello world
                String message = "Hello World";
                channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            };
        });
    }

    @Test
    public void produce2() {
        send(channel -> {

            // 发送一条持久化的消息：hello world
            String message = "Hello World";
            try {
                channel.basicPublish(EXCHANGE_NAME,"", true,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        "mandatory test".getBytes());
                channel.addReturnListener(
                        new ReturnListener(){
                            @Override
                            public void handleReturn(int replyCode, String replyText,
                                                     String exchage, String routingKey,
                                                     AMQP.BasicProperties basicProperties,
                                                     byte[] body) throws IOException{

                                String message = new String(body);
                                System.out.println("Basic.Return 返回的结果是:"+message);
                            }
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
            };
        });
    }

}
