package com.banbi.rpc.test;

import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.transport.RpcServer;

public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        // 注册HelloServiceImpl服务
        rpcServer.register(helloService, 9000);
    }
}
