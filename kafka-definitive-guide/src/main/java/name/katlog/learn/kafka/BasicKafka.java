package name.katlog.learn.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by fw on 2020/4/20
 */
public class BasicKafka {

    protected static KafkaConsumer<String, String> defaultConsumer;

    static {
        Properties props = new Properties();
        props.put("bootstrap.servers", "broker1:9092,broker2:9092");
        props.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        defaultConsumer = new KafkaConsumer<String, String>(props);
    }

    protected static KafkaConsumer<String, String> quickConsumer(Map<String, String> args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "broker1:9092,broker2:9092");
        props.put("key.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",   "org.apache.kafka.common.serialization.StringDeserializer");
        if (args != null) {
            args.forEach(props::put);
        }
        return new KafkaConsumer<>(props);
    }


    protected KafkaConsumer<String, String> notAutoCommitOffsetConsumer() {
        // 将auto.commit.offset 设为 false，
        Map<String,String > arg = new HashMap<>();
        arg.put("auto.commit.offset", "false");
        return quickConsumer(arg);
    }
}
