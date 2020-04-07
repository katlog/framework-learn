package rabbit._02chintroduction;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static rabbit.ConfigConstants.*;
import static rabbit.ConfigConstants.PORT;
import static rabbit.ConfigConstants.USER_NAME;

public class RabbitProducerTopic {



    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);

        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String exchangeName = "topic_exchange_demo";

        channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC, true, false, null);

        channel.queueDeclare("topic_queue1", true, false, false, null);
        channel.queueBind("topic_queue1", exchangeName, "*.rabbitmq.*");


        channel.queueDeclare("topic_queue2", true, false, false, null);
        channel.queueBind("topic_queue2", exchangeName, "*.*.client");
        channel.queueBind("topic_queue2", exchangeName, "com.#");

        channel.basicPublish(exchangeName, "com.rabbitmq.client"
                ,MessageProperties.PERSISTENT_TEXT_PLAIN, "com.rabbitmq.client-message".getBytes());

        channel.basicPublish(exchangeName, "com.hidden.client"
                ,MessageProperties.PERSISTENT_TEXT_PLAIN, "com.hidden.client-message".getBytes());

        channel.basicPublish(exchangeName, "com.hidden.demo"
                ,MessageProperties.PERSISTENT_TEXT_PLAIN, "com.hidden.demo-message".getBytes());

        channel.basicPublish(exchangeName, "java.rabbitmq.demo"
                ,MessageProperties.PERSISTENT_TEXT_PLAIN, "java.rabbitmq.demo-message".getBytes());

        channel.basicPublish(exchangeName, "java.util.concurrent"
                ,MessageProperties.PERSISTENT_TEXT_PLAIN, "java.util.concurrent-message".getBytes());


        channel.close();
        connection.close();
    }
}
