package com.banbi.rpc.test;

import com.banbi.rpc.annotation.ServiceScan;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.serializer.ProtostuffSerializer;
import com.banbi.rpc.transport.RpcServer;
import com.banbi.rpc.transport.netty.server.NettyServer;

@ServiceScan
public class NettyTestServer {
    public static void main(String[] args) {
        RpcServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();
    }
}
