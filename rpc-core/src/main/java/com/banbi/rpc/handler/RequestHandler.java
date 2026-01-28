package com.banbi.rpc.handler;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.ResponseCode;
import com.banbi.rpc.provider.ServiceProvider;
import com.banbi.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



public class RequestHandler{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * handle 是“处理 RPC 请求的总入口”，负责调度、日志、异常处理，并把执行结果返回
     * @param rpcRequest：一次Rpc请求
     * @return
     */
    public Object handle(RpcRequest rpcRequest){
        Object result = null;
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        try {
            // 执行真正的方法调用
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务：{}成功调用方法：{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        }
        catch (IllegalAccessException | InvocationTargetException e){
            logger.info("调用或发送时由错误发送" + e);
        }
        return RpcResponse.success(result, rpcRequest.getRequestId());
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
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
