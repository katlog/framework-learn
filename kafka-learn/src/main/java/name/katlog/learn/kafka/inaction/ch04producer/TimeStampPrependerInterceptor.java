package name.katlog.learn.kafka.inaction.ch04producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * Created by fw on 2021/5/10
 */
public class TimeStampPrependerInterceptor implements ProducerInterceptor<String ,String> {
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        return new ProducerRecord<>(
                record.topic(), record.partition(), record.timestamp(), record.key(), System.currentTimeMillis() + "," + record.value());
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
