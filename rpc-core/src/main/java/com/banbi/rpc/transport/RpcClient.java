package com.banbi.rpc.transport;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.serializer.CommonSerializer;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);

    void setSerializer(CommonSerializer serializer);
}