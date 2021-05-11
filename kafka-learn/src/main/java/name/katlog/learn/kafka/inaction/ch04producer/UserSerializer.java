package name.katlog.learn.kafka.inaction.ch04producer;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by fw on 2021/5/11
 */
public class UserSerializer implements Serializer {

    private ObjectMapper objectMapper;
    @Override
    public void configure(Map configs, boolean isKey) {
        objectMapper= new ObjectMapper();
    }

    @Override
    public byte[] serialize(String topic, Object data) {
        byte[] ret = null;
        try {
            ret = objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void close() {
    }
}
