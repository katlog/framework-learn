package name.katlog.learn.kafka.definitiveguid.ch04consumer;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import name.katlog.learn.kafka.definitiveguid.BasicKafka;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fw on 2020/4/20
 */
@Slf4j
public class OnPartitionsAssigned extends BasicKafka {

    private KafkaConsumer<String, String> consumer = notAutoCommitOffsetConsumer();
    private Map<TopicPartition,OffsetAndMetadata> currentOffsets = new HashMap<>();

    class HandleRebalance implements ConsumerRebalanceListener {
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

        }
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            System.out.println("Lost partitions in rebalance.  Committing current  offsets:" + currentOffsets);
            consumer.commitSync(currentOffsets);
        }
    }


    @Test
    public void onPartitionsAssigned(){
        List<String> topics = Lists.newArrayList("consumers");

        try {
            consumer.subscribe(topics, new HandleRebalance());
            while (true) {
                ConsumerRecords<String, String> records =  consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records)         {
                    System.out.printf("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n"
                            , record.topic(), record.partition(), record.offset(),record.key(), record.value());
                    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()+1, "no metadata"));
                }
                consumer.commitAsync(currentOffsets, null);
            }
        } catch (WakeupException e) {     // 忽略异常，正在关闭消费者
        } catch (Exception e) {     log.error("Unexpected error", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
                System.out.println("Closed consumer and we are done");
            }
        }
    }
}
