package rabbit._09highlevel;

import org.junit.Test;
import rabbit.BaseRabbit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**  惰性队列 */
public class RabbitMqLazyQueue extends BaseRabbit {


    /** 惰性队列 */
    @Test
    public void lazyQueueDefine1() throws IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-queue-mode", "lazy");
        defaultChannel.queueDeclare("myqueue", false, false, false, args);
    }

}
