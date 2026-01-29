package com.banbi.test;

import com.banbi.rpc.api.ByeService;
import com.banbi.rpc.api.HelloObject;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.loadbalancer.RoundRobinLoadBalancer;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.RpcClientProxy;
import com.banbi.rpc.transport.socket.client.SocketClient;

public class SocketTestClient {
    public static void main(String[] args) {
        SocketClient client = new SocketClient(CommonSerializer.KRYO_SERIALIZER, new RoundRobinLoadBalancer());
//        client.setSerializer(new HessianSerializer());
        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is test message");
        //由动态代理可知，代理对象调用hello()实际会执行invoke()
        for(int i = 0; i < 20; i++){
            //由动态代理可知，代理对象调用hello()实际会执行invoke()
            String res = helloService.hello(object);
            System.out.println(res);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
