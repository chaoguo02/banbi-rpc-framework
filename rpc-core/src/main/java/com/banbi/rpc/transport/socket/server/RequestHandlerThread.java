package com.banbi.rpc.transport.socket.server;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.handler.RequestHandler;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.transport.socket.util.ObjectReader;
import com.banbi.rpc.transport.socket.util.ObjectWriter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 服务端用一个线程（或线程池中的一个任务）来处理“某个客户端 socket 连接上的一次 RPC 调用请求”，并把调用结果写回给客户端
 */
@AllArgsConstructor
public class RequestHandlerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;

    private RequestHandler requestHandler;

    private CommonSerializer serializer;

    // 服务器异步调用
    @Override
    public void run() {
        try (InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();){
            // 从客户端读取Rpc请求对象rpcRequest
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(is);
            // 调用invoke执行目标方法
            Object response = requestHandler.handle(rpcRequest);
            // 将结果包装为RpcResponse，并写给客户端
            ObjectWriter.writeObject(os, response, serializer);
        }catch (IOException e){
            logger.info("调用或发送时发生错误： " + e);
        }
    }
}
