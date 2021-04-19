package name.katlog.learn.kafka.inaction.ch02devhistory;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by fw on 2021/4/6
 */
public class KafkaNewVer {

    @Test
    public void newVer_producer(){

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String ,String > producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
        }
        producer.close();
    }

    @Test
    public void oldVer_producer(){

        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092,localhost:9093,localhost:9094");
        props.put("serializer.class", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        Producer<String ,String > producer = new KafkaProducer<>(props);

        // KeyedMessage<String,String> msg = new KeyedMessage<>("my-topic","hello world");
        // producer.send(msg);
    }

    @Test
    public void newVer_consumer(){

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enabled.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String ,String > consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList("foo", "bar"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset=%d, key=%s, value=%s ", record.offset(), record.key(), record.value());
            }
        }

    }
}
