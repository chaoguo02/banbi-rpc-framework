package com.banbi.rpc.transport.socket.server;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.registry.ServiceRegistry;
import com.banbi.rpc.handler.RequestHandler;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 服务端用一个线程（或线程池中的一个任务）来处理“某个客户端 socket 连接上的一次 RPC 调用请求”，并把调用结果写回给客户端
 */
@AllArgsConstructor
public class RequestHandlerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;

    private RequestHandler requestHandler;

    private ServiceRegistry serviceRegistry;

    // 服务器异步调用
    @Override
    public void run() {
        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){
            // 从客户端读取Rpc请求对象rpcRequest
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            // 根据接口名从注册中心找到服务实现对象
            Object service = serviceRegistry.getService(interfaceName);
            // 调用invoke执行目标方法
            Object result = requestHandler.handle(rpcRequest, service);
            // 将结果包装为RpcResponse，并写给客户端
            oos.writeObject(RpcResponse.success(result));
            oos.flush();
        }catch (IOException | ClassNotFoundException e){
            logger.info("调用或发送时发生错误： " + e);
        }
    }
}
