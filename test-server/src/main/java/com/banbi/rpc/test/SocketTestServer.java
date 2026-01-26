package com.banbi.rpc.test;

import com.banbi.rpc.api.ByeService;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.registry.DefaultServiceRegistry;
import com.banbi.rpc.registry.ServiceRegistry;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.socket.client.SocketClient;
import com.banbi.rpc.transport.socket.server.SocketServer;

public class SocketTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ByeService byeService = new ByeServiceImpl();

        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();

        serviceRegistry.register(helloService);
        serviceRegistry.register(byeService);

        SocketServer socketServer = new SocketServer(serviceRegistry);
        socketServer.setSerializer(new KryoSerializer());
        socketServer.start(9000);
    }
}
