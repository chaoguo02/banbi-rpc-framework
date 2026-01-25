package com.banbi.rpc.transport;

import com.banbi.rpc.entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /*
    Java的序列化方式：通过Socket传输。创建一个Socket，获取oos对象，将需要发送的对象传进去
    接收时，获取ois对象，调用readObject()方法获得返回的对象
     */
    public Object sendRequest(RpcRequest rpcRequest, String host, int port){
        try (Socket socket = new Socket(host, port)){
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(rpcRequest);
            oos.flush();
            return ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            logger.error("调用时有错误发生：" + e);
            return null;
        }
    }
}

