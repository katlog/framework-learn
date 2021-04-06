package name.katlog.learn.kafka.definitiveguid.ch04consumer;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import name.katlog.learn.kafka.definitiveguid.BasicKafka;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

/**
 * Created by fw on 2020/4/20
 */
@Slf4j
public class KafkaConsumerTest extends BasicKafka {


    @Test
    public void createKafkaConsumer(){
        // 1. 创建kafka消费者
        Properties props = new Properties();
        props.put("bootstrap.servers", "broker1:9092,broker2:9092");
        props.put("group.id", "CountryCounter");
        props.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // 2. 监听主题
        consumer.subscribe(Collections.singleton("cosutomerContries"));

        // 3.轮训消息
        Map<String,Integer> custCountryMap = new HashMap<>();
        try {
            while (true) {
                // 每条记录都包含所属主题信息、所在分区信息、分区里的偏移量，以及记录的键值对
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.debug("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n"
                            , record.topic(), record.partition(), record.offset(),record.key(), record.value());
                    int updatedCount = 1;
                    if (custCountryMap.containsKey(record.value())) {
                        updatedCount = custCountryMap.get(record.value()) + 1;
                    }
                    custCountryMap.put(record.value(), updatedCount);
                    System.out.println(JSON.toJSONString(custCountryMap));
                }
            }

        }finally {
            // 会主动引发再均衡，而非等群组协调器发现其不再发送心跳时
            consumer.close();
        }
    }

    /** 提交偏移量 */
    @Test
    public void commitOffsetSync(){

        // 将auto.commit.offset 设为 false，
        KafkaConsumer<String, String> consumer = notAutoCommitOffsetConsumer();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset =%d, customer = %s, country = %s\n"
                        ,record.topic(), record.partition(),record.offset(), record.key(), record.value());
            }
            try {
                consumer.commitSync();
            } catch (CommitFailedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void commitOffsetAsyn(){
        // 将auto.commit.offset 设为 false，
        KafkaConsumer<String, String> consumer = notAutoCommitOffsetConsumer();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records)     {
                System.out.printf("topic = %s, partition = %s,offset = %d, customer = %s, country = %s\n"
                        ,record.topic(), record.partition(), record.offset(),record.key(), record.value());
            }
            consumer.commitAsync();
        }
    }

    @Test
    public void commitOffsetAsycnCallback(){
        KafkaConsumer<String, String> consumer = notAutoCommitOffsetConsumer();


        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s,offset = %d, customer = %s, country = %s\n"
                        ,record.topic(), record.partition(), record.offset(),record.key(), record.value());
            }
            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition,OffsetAndMetadata> offsets, Exception e) {
                    if (e != null)
                        log.error("Commit failed for offsets {}", offsets, e);
                }
            });
        }
    }

    @Test
    public void commitOffsetSyncAsync(){

        KafkaConsumer<String, String> consumer = notAutoCommitOffsetConsumer();
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = %d,customer = %s, country = %s\n"
                            ,record.topic(), record.partition(),record.offset(), record.key(), record.value());
                }
                consumer.commitAsync();
            }
        } catch (Exception e) {
            log.error("Unexpected error", e);
        } finally {
            try {         consumer.commitSync();
            } finally {         consumer.close();     }
        }
    }


}
