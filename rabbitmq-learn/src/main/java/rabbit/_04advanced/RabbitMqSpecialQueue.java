package rabbit._04advanced;

import org.junit.Test;
import rabbit.BaseRabbit;

/**  测试特殊队列 */
public class RabbitMqSpecialQueue extends BaseRabbit {

    /** 死信队列： */
    @Test
    public void dlxQueue_1(){

        // defaultChannel.queueDeclare("")
    }

    /** 延迟队列 */
    @Test
    public void delayQueue(){

    }

    /** 优先级队列 */
    @Test
    public void priorityQueue(){

    }

}
