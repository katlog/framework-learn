package name.katlog.learn.kafka.inaction.ch04producer;

/**
 * Created by fw on 2021/5/11
 */
public class User {

    private String firstName;
    private String lastName;
    private int age;
    private String address;

    public User(String firstName, String lastName, int age, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.address = address;
    }
}
