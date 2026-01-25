package com.banbi.rpc.transport;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

@AllArgsConstructor
public class WorkerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    private Socket socket;

    private Object service;

    @Override
    public void run() {
        try(ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())){
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            oos.writeObject(RpcResponse.success(returnObject));
            oos.flush();
        }catch(IOException | ClassNotFoundException | NoSuchMethodException
               | IllegalAccessException | InvocationTargetException e){
            logger.info("调用或发送时有错误发生" + e);
        }
    }
}
