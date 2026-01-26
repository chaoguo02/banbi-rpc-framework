package com.banbi.rpc.transport;

import com.banbi.rpc.entity.RpcRequest;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);
}