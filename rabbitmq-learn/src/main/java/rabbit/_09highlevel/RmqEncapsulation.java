package rabbit._09highlevel;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQBasicProperties;
import lombok.ToString;
import org.junit.Test;

import java.io.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class RmqEncapsulation {

    private static String host = "localhost";
    private static int port = 5672;
    private static String vhost = "/";
    private static String userName = "guest";
    private static String passWord = "guest";

    private static Connection connection;
    // 分片数，表示一个逻辑队列背后的实际队列数量
    private int subdivsionNum;
    private ConcurrentLinkedQueue<Message> blockingQueue;

    public RmqEncapsulation(int subdivsionNum) {
        this.subdivsionNum = subdivsionNum;
        blockingQueue = new ConcurrentLinkedQueue<>();
    }

    public static void newConnetion() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setVirtualHost(vhost);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(passWord);

        connection = connectionFactory.newConnection();
    }
    public static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            newConnetion();
        }
        return connection;
    }

    public static void closeConnection() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    public void exchangeDeclare(Channel channel, String exchang, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        channel.exchangeDeclare(exchang, type, durable, autoDelete, arguments);
    }

    public void queueDeclare(Channel channel, String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        for (int i = 0; i < subdivsionNum; i++) {
            String queueName = queue + "_" + i;
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        }
    }

    public void queueBind(Channel channel, String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        for (int i = 0; i < subdivsionNum; i++) {
            String queueName = queue + "_" + i;
            String rkName = routingKey + "_" + i;
            channel.queueBind(queueName, exchange, rkName, arguments);
        }
    }

    @Test
    public  void producer(){
        RmqEncapsulation rmqEncapsulation = new RmqEncapsulation(4);
        try {
            Connection connection = RmqEncapsulation.getConnection();
            Channel channel = connection.createChannel();
            rmqEncapsulation.exchangeDeclare(channel, "exchange", "direct", true, false, null);
            rmqEncapsulation.queueDeclare(channel, "queue", true, false, false, null);
            rmqEncapsulation.queueBind(channel, "queue", "exchange", "rk", null);


        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }finally {
            try {
                RmqEncapsulation.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void consumer() throws IOException, TimeoutException {
        RmqEncapsulation rmqEncapsulation = new RmqEncapsulation(4);
        Connection connection = RmqEncapsulation.getConnection();

        // basicPublish方法的使用示例如下
        Channel channel = connection.createChannel();
        for (int i = 0; i < 100; i++) {
            Message message = new Message();
            message.setMsgSeq(i);
            message.setMsgBody("rabbit mq encapsulation ");
            byte[] body = getBytesFromObject(message);
            rmqEncapsulation.basicPublish(channel, "exchange", "rk", false, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
        }
    }

    public void basicPublish(Channel channel, String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException {

        Random random = new Random();
        int index = random.nextInt(subdivsionNum);
        String rkName = routingKey + "_" + index;
        channel.basicPublish(exchange, routingKey, mandatory, props,body);
    }


    public static byte[] getBytesFromObject(Object object) throws IOException {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(object);
        oo.close();
        bo.close();
        return bo.toByteArray();
    }
    public static Object getObjectFromBytes(byte[] body) throws IOException, ClassNotFoundException {
        if (body == null || body.length < 1) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(body);
        ObjectInputStream oi = new ObjectInputStream(bi);
        oi.close();
        bi.close();
        return oi.readObject();
    }

    public GetResponse basicGet(Channel channel, String queue, boolean autoAck) throws IOException {
        GetResponse getResponse = null;
        Random random = new Random();
        int index = random.nextInt(subdivsionNum);
        getResponse = channel.basicGet(queue + "_" + index, autoAck);
        if (getResponse == null) {
            for (int i = 0; i < subdivsionNum; i++) {
                String queueName = queue + "_" + i;
                getResponse = channel.basicGet(queueName, autoAck);
                if (getResponse != null) {
                    return getResponse;
                }
            }
        }
        return getResponse;
    }


}
