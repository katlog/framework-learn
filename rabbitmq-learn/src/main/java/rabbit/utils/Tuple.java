package rabbit.utils;

import lombok.Data;

@Data
public class Tuple<A,B> {
    private A a;
    private B b;
}
