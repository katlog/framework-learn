import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by fw on 2019/6/17
 */
public class NIO {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            ServerSocketChannel serverSocketChannel = serverSocket.getChannel();

            serverSocketChannel.register(Selector.open(), SelectionKey.OP_CONNECT);


            SocketChannel socketChannel = serverSocketChannel.accept();



        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
