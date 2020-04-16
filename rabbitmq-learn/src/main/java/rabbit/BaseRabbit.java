package rabbit;

import com.rabbitmq.client.*;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BaseRabbit {

    protected static final ConnectionFactory FACTORY = new ConnectionFactory();
    protected Connection defaultConnection;
    protected Channel defaultChannel;
    protected static HttpClient client;

    @BeforeClass
    public static void  beforeClass(){
        initFactory(FACTORY);
    }

    protected static void initFactory( ConnectionFactory factory ) {
        System.out.println("factory init begin----------------" );

        factory.setPassword(ConfigConstants.PASSWORD);
        factory.setUsername(ConfigConstants.USER_NAME);

        factory.setHost(ConfigConstants.IP_ADDRESS);
        factory.setPort(ConfigConstants.PORT);
        System.out.println("factory init end----------------" );

        client =  HttpClientBuilder.create().build();
    }


    @Before
    public void before() throws IOException, TimeoutException {
        defaultConnection = FACTORY.newConnection();
        defaultChannel = defaultConnection.createChannel();
        System.out.println("created default connection = " + defaultConnection);
        System.out.println("created default channel= " + defaultChannel);
    }

    @After
    public void after() {
        try {
            defaultChannel.close();
            System.out.println("default channel  closed= " + defaultChannel);
            defaultConnection.close();
            System.out.println("default connection  closed= " + defaultConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void publishDurable(Meta meta, String message) throws IOException {
        publishDurable(meta.getExchange(), meta.getRoutingKey(), message);
    }

    protected void publishDurable(String exchangeName,String routingKey,String message) throws IOException {
        defaultChannel.basicPublish(exchangeName,routingKey
                ,new AMQP.BasicProperties()
                        .builder()
                        .contentType("text/plain")
                        .deliveryMode(2)
                        .build()
                ,message.getBytes());
    }

    /**  快速声明 */
    protected Meta quickDeclareExchangeQueue(String prefix) throws IOException {
        return quickDeclareExchangeQueue(prefix, false,true);
    }

    protected Meta quickDeclareExchangeQueue(String prefix,boolean durable,boolean autoDelete) throws IOException {

        String queue = prefix + "_queue";
        String exchange = prefix + "_exchange_demo";
        String routingKey = prefix + "_routing_key";
        defaultChannel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, durable, autoDelete, null);
        defaultChannel.queueDeclare(queue, durable, false, autoDelete, null);
        defaultChannel.queueBind(queue, exchange, routingKey, null);

        return new Meta(exchange, queue, routingKey);
    }


}
