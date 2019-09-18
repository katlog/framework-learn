import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by fw on 2019/6/17
 */
public class BlockingIo {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(80,10);
            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("socket = " + socket.getOutputStream());
                            InputStream inputStream = socket.getInputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
