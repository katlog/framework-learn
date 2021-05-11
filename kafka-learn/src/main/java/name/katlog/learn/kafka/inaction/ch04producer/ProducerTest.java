package name.katlog.learn.kafka.inaction.ch04producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerTest {


    /** 4.2 构造producer */
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

        props.put("compression.type", "lz4");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");


        props.put("max.block.ms", 3000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);

        props.put("max.request.size", 10485760);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760);

        props.put("request.timeout.ms", 60000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
    }


    /** 4.3 消息分区 */
    @Test
    public void _useDefinedPartition() throws ExecutionException, InterruptedException {
        Properties props = new Properties();

        props.put("partitioner.class", AuditPartitioner.class.getCanonicalName());
        // 或
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, AuditPartitioner.class.getCanonicalName());


        String topic = "test-topic";
        Producer<String,String> producer = new KafkaProducer<String, String>(new Properties());
        ProducerRecord<String,String> nonKeyRecord = new ProducerRecord<>(topic, "non-key record");
        ProducerRecord<String,String> auditRecord = new ProducerRecord<>(topic, "audit","audit record");
        ProducerRecord<String,String> nonAuditRecord = new ProducerRecord<>(topic, "other","non-audit record");
        producer.send(nonKeyRecord).get();
        producer.send(nonAuditRecord).get();
        producer.send(auditRecord).get();
        producer.send(nonKeyRecord).get();
        producer.send(nonAuditRecord).get();

        // kafka-run-class.sh kafka.tools.GetOffsetShell  --broker-list localhost:9092 --topic test-topic 可查询每个分区的消息数
    }

    /** 4.4 消息序列化 */
    @Test
    public void serializer() throws ExecutionException, InterruptedException {
        Properties props = new Properties();

        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 或
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        String topic = "test-topic";
        Producer<String,User> producer = new KafkaProducer<String, User>(props);

        User user = new User("XI", "HU", 33, "beijing, china");
        ProducerRecord<String,User> record = new ProducerRecord<>(topic, user);
        producer.send(record).get();
        producer.close();

    }


    /** 4.5 构建拦截器 */
    @Test
    public void interceptor() throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        // ... props.put(..)
        // 构建拦截器
        List<String> interceptors = new ArrayList<>();
        interceptors.add(TimeStampPrependerInterceptor.class.getCanonicalName());
        interceptors.add(CounterInterceptor.class.getCanonicalName());
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);
        // ...
        String topic = "test-topic";

        Producer<String,String> producer = new KafkaProducer<String, String>(props);
        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "message" + i);
            producer.send(record).get();
        }
        // 一定要关闭producer，这样才会调用interceptor的close方法
        producer.close();
    }
}
