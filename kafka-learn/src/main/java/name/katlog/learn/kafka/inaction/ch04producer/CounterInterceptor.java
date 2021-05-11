package name.katlog.learn.kafka.inaction.ch04producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * Created by fw on 2021/5/10
 */
public class CounterInterceptor implements ProducerInterceptor<String ,String> {

    private int errorCounter = 0;
    private int successCounter = 0;

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            successCounter++;
        } else {
            errorCounter++;
        }
    }

    @Override
    public void close() {
        System.out.println("Successful sent = " + successCounter);
        System.out.println("Failed sent = " + errorCounter);
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
