package rabbit._09highlevel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Message implements Serializable {

    private static final long serialVersionUID = 6159162287326801934L;
    private long msgSeq;
    private String msgBody;
    private long deliveryTag;

}
