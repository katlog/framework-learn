package rabbit._09highlevel;

public interface IMsgCallback {
    ConsumeStatus consumeMsg(Message message);
}
