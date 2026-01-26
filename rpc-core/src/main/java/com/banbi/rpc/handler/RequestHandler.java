package com.banbi.rpc.handler;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



public class RequestHandler{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * handle 是“处理 RPC 请求的总入口”，负责调度、日志、异常处理，并把执行结果返回
     * @param rpcRequest：一次Rpc请求
     * @param service:真正的服务实现对象
     * @return
     */
    public Object handle(RpcRequest rpcRequest, Object service){
        Object result = null;
        try {
            // 执行真正的方法调用
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务：{}成功调用方法：{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        }
        catch (IllegalAccessException | InvocationTargetException e){
            logger.info("调用或发送时由错误发送" + e);
        }
        return result;
    }

    /**
     * 根据请求反射找到方法并调用 invoke
     * @param rpcRequest
     * @param service
     * @return
     */
    public Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws InvocationTargetException, IllegalAccessException{
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        }catch(NoSuchMethodException e){
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
