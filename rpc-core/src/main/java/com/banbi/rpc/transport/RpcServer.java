package com.banbi.rpc.transport;

import com.banbi.rpc.serializer.CommonSerializer;

public interface RpcServer {
    void start(int port);

    void setSerializer(CommonSerializer serializer);
}