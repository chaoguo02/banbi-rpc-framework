package com.banbi.rpc.util;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.ResponseCode;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class RpcManagerChecker {

    private static final Logger logger = LoggerFactory.getLogger(RpcManagerChecker.class);

    private static final String INTERFACE_NAME = "interfaceName";

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse){
        if(rpcResponse == null){
            logger.error("调用服务失败，serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        if(rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())){
            logger.error("调用服务失败，serviceName：{},RpcResponse:{}",rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        logger.info("rpcRequest的requestId：{} ，rpcResponse的requestId：{}",rpcRequest.getRequestId(), rpcResponse.getRequestId());
    }
}
