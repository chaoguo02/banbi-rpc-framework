package com.banbi.rpc.transport;

import com.banbi.rpc.serializer.CommonSerializer;

public interface RpcServer {
    void start();

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;
    /*
    向Nacos注册服务
     */
    <T> void publishService(T service, Class<T> serviceClass);
}