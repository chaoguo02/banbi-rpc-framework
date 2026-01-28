package com.banbi.rpc.test;

import com.banbi.rpc.api.ByeService;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.socket.server.SocketServer;

public class SocketTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl2();
        SocketServer server = new SocketServer("127.0.0.1", 9998);
        server.setSerializer(new HessianSerializer());
        server.publishService(helloService, HelloService.class);
    }
}
