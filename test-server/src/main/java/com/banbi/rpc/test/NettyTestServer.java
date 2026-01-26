package com.banbi.rpc.test;

import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.registry.DefaultServiceRegistry;
import com.banbi.rpc.registry.ServiceRegistry;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.ProtostuffSerializer;
import com.banbi.rpc.transport.netty.server.NettyServer;

public class NettyTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        NettyServer server = new NettyServer();
        server.setSerializer(new ProtostuffSerializer());
        server.start(9999);
    }
}
