package com.banbi.rpc.test;

import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.serializer.ProtostuffSerializer;
import com.banbi.rpc.transport.netty.server.NettyServer;

public class NettyTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer server = new NettyServer("127.0.0.1", 9999);
        server.setSerializer(new ProtostuffSerializer());
        server.publishService(helloService, HelloService.class);
    }
}
