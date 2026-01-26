package com.banbi.test;

import com.banbi.rpc.api.ByeService;
import com.banbi.rpc.api.HelloObject;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.transport.RpcClientProxy;
import com.banbi.rpc.transport.socket.client.SocketClient;

public class SocketTestClient {
    public static void main(String[] args) {
        SocketClient client = new SocketClient("127.0.0.1", 9000);

        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        ByeService byeService = proxy.getProxy(ByeService.class);

        HelloObject object = new HelloObject(12, "This is test message");
        //由动态代理可知，代理对象调用hello()实际会执行invoke()
        String res = helloService.hello(object);
        String res2 = byeService.bye("banbi");
        System.out.println(res);
        System.out.println(res2);
    }
}
