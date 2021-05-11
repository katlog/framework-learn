package io.netty.cases.chapter.demo14;

import io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.NettyServerProvider;

/**
 * Created by fw on 2021/4/23
 */
public class GRPCServer {
    public static void main(String[] args) {
        NettyServerBuilder nettyServerBuilder = NettyServerBuilder.forPort(8089);
    }
}
