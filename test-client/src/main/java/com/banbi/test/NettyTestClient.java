package com.banbi.test;

import com.banbi.rpc.api.HelloObject;
import com.banbi.rpc.api.HelloService;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.serializer.ProtostuffSerializer;
import com.banbi.rpc.transport.RpcClient;
import com.banbi.rpc.transport.RpcClientProxy;
import com.banbi.rpc.transport.netty.client.NettyClient;

public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
//        client.setSerializer(new HessianSerializer());
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(15,"this is for u");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
