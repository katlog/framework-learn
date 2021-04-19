package name.katlog.learn.kafka.inaction.ch05consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by fw on 2021/4/8
 */
public class ConsumerTest {

    public static void main(String[] args) {
        String topicName = "test-topic";
        String groupID = "test-group";

        Properties props = new Properties();
        // 下面四项必须指定
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", groupID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("enabled.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        //从最早的消息开始读取
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String,String> consumer =new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topicName));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s%n"
                            , record.offset(), record.key(), record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }

    @Test
    public void constructConsumer(){
        Properties props = new Properties();
        KafkaConsumer<String,String> consumer =new KafkaConsumer<String, String>(props,new StringDeserializer(), new StringDeserializer());
    }

    /** 订阅多个主题 */
    @Test
    public void consumerSubscribeTopics(){
        Properties props = new Properties();

        KafkaConsumer<String,String> consumer =new KafkaConsumer<>(props);

        /** consumer订阅是延迟生效的，即订阅信息只有在下次poll调用时才会正式生效  */

        // 订阅topic列表
        consumer.subscribe(Arrays.asList("topic1","topic2","topic3"));

        // 基于正则订阅topic
        consumer.subscribe(Pattern.compile("kafka.*"),new NoOpConsumerRebalanceListener());

        // 若用独立consumer，可手动订阅
        TopicPartition tp1 = new TopicPartition("topic-name", 0);
        TopicPartition tp2 = new TopicPartition("topic-name", 1);
        consumer.assign(Arrays.asList(tp1, tp2));

    }

    /**  consumer 程序除消费消息外没其他的定时任务需要执行 */
    @Test
    public void consumerRecordWithoutTimeTask(){
        KafkaConsumer<String,String> consumer =new KafkaConsumer<>(new HashMap<>());

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s%n"
                            , record.offset(), record.key(), record.value());
                }
            }
        }catch (WakeupException e){
            // 此处忽略异常的处理
        }finally {
            consumer.close();
        }
    }
}


