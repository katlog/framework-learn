package rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meta {
    private String exchange;
    private String queue;
    private String routingKey;
}
