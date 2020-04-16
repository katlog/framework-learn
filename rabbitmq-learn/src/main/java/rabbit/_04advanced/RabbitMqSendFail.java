package rabbit._04advanced;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import com.rabbitmq.tools.json.JSONUtil;
import org.junit.Test;
import rabbit.BaseRabbit;
import rabbit.Meta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RabbitMqSendFail extends BaseRabbit {



    /**  找不到队列时 */
    @Test
    public void publish_mandtory() throws IOException, InterruptedException {

        String exchange = "publish_mandtory";
        String routingKey = "routing-key";
        String message = "never arrive msg ...";

        // 找不到交换器时  不会回调【找不到交换器时channel会关闭】
        // System.out.println("ready to send msg to = " + message + ", exchange Name:" + exchange);
        // defaultChannel.basicPublish(exchange, routingKey, true, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());

        // 找到交换器  找不到队列
        Meta meta = quickDeclareExchangeQueue(exchange);
        System.out.println("ready to send msg to = " + message + ", exchange name:" + meta.getExchange());
        //服务端会返回	Basic.Return Content-Header type=text/plain Content-Body  (text/plain)
        // 正常发送不会返回 Basic.Return
        defaultChannel.basicPublish(meta.getExchange(), "", true, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());

        defaultChannel.addReturnListener(new ReturnCallback() {
            @Override
            public void handle(Return returnMessage) {
                String returnMsg = JSON.toJSONString(returnMessage);
                System.out.println("returnMsg = " + returnMsg);
                System.out.println(new String(returnMessage.getBody()));
            }
        });

        Thread.sleep(6000);
    }

    @Test
    public void publish_immediate() throws IOException, InterruptedException {
        Meta meta = quickDeclareExchangeQueue("publish_immediate1");

        // 队列上没有消费者时 会直接返回 Connection.Close reply=NOT_IMPLEMENTED - immediate=true
        // 3.0之后有消费者好像也不行 直接时服务端返回的 not_implemented 状态
        // 此时connection的状态为closed，但channel的状态还是open
        defaultChannel.basicPublish(meta.getExchange(), meta.getRoutingKey(), false, true
                , MessageProperties.PERSISTENT_TEXT_PLAIN, "publish immediate msg..".getBytes());

        Thread.sleep(60000);
    }


    /**  备份交换器 :会使得 mandatory无效*/
    @Test
    public void publish_alternateExchange() throws IOException {

        Map<String, Object> args = new HashMap<>();
        // 设置备胎交换器
        args.put("alternate-exchange", "myAe");
        defaultChannel.exchangeDeclare("normalExchange", "direct", true, false, args);
        defaultChannel.queueDeclare("normalQueue", true, false, false, null);
        defaultChannel.queueBind("normalQueue", "normalExchange", "normalKey");

        // 备胎交换器
        defaultChannel.exchangeDeclare("myAe", "fanout", true, false, null);
        defaultChannel.queueDeclare("unroutedQueue", true, false, false, null);
        defaultChannel.queueBind("unroutedQueue", "myAe", "");

        // 发送：正常路由
        System.out.println("send normally .... " );
        publishDurable("normalExchange","normalKey","alternate exchange normally arrived..");

        // 发送：异常路由 （备份交换器 使mandatory无效）
        System.out.println("send unusually .... " );
        defaultChannel.basicPublish("normalExchange","normalWrongKey",true
                ,MessageProperties.PERSISTENT_TEXT_PLAIN,"alternate exchange unusually arrived..".getBytes());

        // 不会进入这个回调
        defaultChannel.addReturnListener(returnMessage -> {
            String returnMsg = JSON.toJSONString(returnMessage);
            System.out.println("returnMsg = " + returnMsg);
            System.out.println(new String(returnMessage.getBody()));
        });
    }
}
