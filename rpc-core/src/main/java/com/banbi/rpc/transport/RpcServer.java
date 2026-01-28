package com.banbi.rpc.transport;

import com.banbi.rpc.serializer.CommonSerializer;

public interface RpcServer {
    void start();

    void setSerializer(CommonSerializer serializer);

    /*
    向Nacos注册服务
     */
    <T> void publishService(T service, Class<T> serviceClass);
}