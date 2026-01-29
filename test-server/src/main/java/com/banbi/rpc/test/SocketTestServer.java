package com.banbi.rpc.test;

import com.banbi.rpc.annotation.ServiceScan;
import com.banbi.rpc.api.ByeService;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.RpcServer;
import com.banbi.rpc.transport.socket.server.SocketServer;

@ServiceScan
public class SocketTestServer {
    public static void main(String[] args) {
        RpcServer server = new SocketServer("127.0.0.1", 9998);
        server.start();
    }
}
