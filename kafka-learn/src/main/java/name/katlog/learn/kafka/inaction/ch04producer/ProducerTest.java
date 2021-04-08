package name.katlog.learn.kafka.inaction.ch04producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerTest {

    public static void main(String[] args) {
        Properties props = new Properties();
        // 以下三项，必须指定
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("acks", "-1");
        props.put("retries", 3);
        props.put("batch.size", 323840);
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", 3000);

        Producer<String,String> producer = new KafkaProducer<String, String>(props);
        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<>("my-topic", "key:" + i, "value:" + i));
        }

        producer.close();
    }

    /**  构造producer时指定 */
    @Test
    public void constructProducerDefineSerializer(){
        Properties props = new Properties();
        Serializer<String> valueSerializer = new StringSerializer();
        Serializer<String> keySerializer = new StringSerializer();
        Producer<String,String> producer = new KafkaProducer<String, String>(props, keySerializer, valueSerializer);
    }

    /**  异步发送 */
    @Test
    public void asyncSend(){
        Producer<String,String> producer = new KafkaProducer<String, String>(new Properties());
        ProducerRecord<String,String> record = new ProducerRecord<>("topic", "value");
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e == null) {
                    // 消息发送成功
                } else {
                    // 消息发送失败
                }
            }
        });
    }

    /**  同步发送 */
    @Test
    public void syncSend() throws ExecutionException, InterruptedException {
        Producer<String,String> producer = new KafkaProducer<String, String>(new Properties());
        ProducerRecord<String,String> record = new ProducerRecord<>("topic", "value");
        producer.send(record).get();
    }

    /**  发送消息中捕获异常 */
    @Test
    public void catchException(){
        Producer<String,String> producer = new KafkaProducer<String, String>(new Properties());
        ProducerRecord<String,String> record = new ProducerRecord<>("topic", "value");
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e == null) {
                    // 消息发送成功
                } else {
                    if (e instanceof RetriableException) {
                        // 处理可重试瞬间异常
                    } else {
                        // 处理不可重试异常
                    }
                }
            }
        });
    }

    /**  配置 */
    @Test
    public void props(){
        Properties props = new Properties();


        props.put("acks", "-1");
        props.put(ProducerConfig.ACKS_CONFIG, "-1");


        props.put("retries", 3);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);


        props.put("batch.size", 323840);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 323840);


        props.put("linger.ms", 10);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);


        props.put("buffer.memory", 33554432);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);


        props.put("max.block.ms", 3000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
    }
}
