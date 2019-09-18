import com.sun.net.httpserver.HttpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by fw on 2019/5/6
 */
public class Test {
    public class PlainOioServer {
        public void serve(int port) throws IOException {
            final ServerSocket socket = new ServerSocket(port);     //1
            try {
                for (; ; ) {
                    final Socket clientSocket = socket.accept();    //2
                    System.out.println("Accepted connection from " + clientSocket);
                    new Thread(new Runnable() {                        //3
                        @Override
                        public void run() {
                            OutputStream out;
                            try {
                                out = clientSocket.getOutputStream();
                                out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8")));    //4
                                out.flush();
                                clientSocket.close();        //5
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    clientSocket.close();
                                } catch (IOException ex) {
                                    // ignore on close
                                }
                            }
                        }
                    }).start();                                        //6
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class PlainNioServer {
        public void serve(int port) throws IOException {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);                                      //1
            Selector selector = Selector.open();                        //2
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);    //3
            final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
            for (;;) {
                try {
                    selector.select();                             //4
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // handle exception
                    break;
                }
                Set<SelectionKey> readyKeys = selector.selectedKeys();    //5
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    try {
                        if (key.isAcceptable()) {                //6
                            ServerSocketChannel server =
                                    (ServerSocketChannel)key.channel();
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_WRITE |
                                    SelectionKey.OP_READ, msg.duplicate());    //7
                            System.out.println(
                                    "Accepted connection from " + client);
                        }
                        if (key.isWritable()) {      //8
                            SocketChannel client =
                                    (SocketChannel)key.channel();
                            ByteBuffer buffer =
                                    (ByteBuffer)key.attachment();
                            while (buffer.hasRemaining()) {
                                if (client.write(buffer) == 0) {        //9
                                    break;
                                }
                            }
                            client.close();   //10
                        }
                    } catch (IOException ex) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException cex) {
                            // 在关闭时忽略
                        }
                    }
                }
            }
        }
    }


    static class HttpServer {
        private final int port;
        public HttpServer(int port) {
            this.port = port;
        }
        public static void main(String[] args) throws Exception {
            if (args.length != 1) {
                System.err.println("Usage: " + HttpServer.class.getSimpleName() +" <port>");
                return;
            }
            int port = Integer.parseInt(args[0]);
            new HttpServer(port).start();
        }
        public void start() throws Exception {
            ServerBootstrap b = new ServerBootstrap();
            NioEventLoopGroup group = new NioEventLoopGroup();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                        @Override
                        public void initChannel(io.netty.channel.socket.SocketChannel ch)
                                throws Exception {
                            System.out.println("initChannel ch:" + ch);
                            ch.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())   // 1
                                    .addLast("encoder", new HttpResponseEncoder())  // 2
                                    // .addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
                                    .addLast("handler", new HttpHandler());        // 4
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            b.bind(port).sync();
        }
    }


    // static class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> { // 1
    static class HttpHandler extends SimpleChannelInboundHandler { // 1
        private AsciiString contentType = HttpHeaderValues.TEXT_PLAIN;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("class:" + msg.getClass().getName());
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer("test".getBytes())); // 2
            HttpHeaders heads = response.headers();
            heads.add(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
            heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes()); // 3
            heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete");
            super.channelReadComplete(ctx);
            ctx.flush(); // 4
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("exceptionCaught");
            if(null != cause) cause.printStackTrace();
            if(null != ctx) ctx.close();
        }
    }



}
