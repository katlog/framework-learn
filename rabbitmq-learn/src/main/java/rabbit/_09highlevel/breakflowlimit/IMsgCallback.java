package rabbit._09highlevel.breakflowlimit;

public interface IMsgCallback {
    ConsumeStatus consumeMsg(Message message);
}
