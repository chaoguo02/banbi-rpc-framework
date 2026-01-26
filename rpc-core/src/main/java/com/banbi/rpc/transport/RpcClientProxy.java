package com.banbi.rpc.transport;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 通过代理对象，返回RpcRequest需要的所有信息
 */

@AllArgsConstructor
public class RpcClientProxy implements InvocationHandler{

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    // 抑制编译器产生警告信息
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        // 创建代理对象
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     *  调用hello方法，实际上就会进入invoke函数
     *  在invoke函数中构造RpcRequest请求，并sendRequest发送请求oos.writeObject()
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法：{}#{}", method.getDeclaringClass().getName(), method.getName());

        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes());
        return client.sendRequest(rpcRequest);
    }
}