package com.banbi.rpc.transport.socket.client;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.ResponseCode;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.transport.RpcClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@AllArgsConstructor
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;

    private final int port;


    /**
     * 在 RPC 客户端侧发起一次远程调用
     * @param rpcRequest
     * @return
     */
    public Object sendRequest(RpcRequest rpcRequest){
        // 建立对服务端的连接（短连接）
        try (Socket socket = new Socket(host, port)){
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // 发送请求到服务端
            oos.writeObject(rpcRequest);
            oos.flush();
            // 阻塞等待并读取客户端响应
            RpcResponse rpcResponse = (RpcResponse) ois.readObject();
            if(rpcResponse == null){
                logger.error("服务调用失败,service:{}" + rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, "service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()){
                logger.error("服务调用失败, service:{} response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, "service:" + rpcRequest.getInterfaceName());
            }
            // 返回真正的业务数据
            return rpcResponse.getData();
        }catch (IOException | ClassNotFoundException e){
            logger.error("调用时有错误发生：" + e);
            throw new RpcException("服务调用失败：", e);
        }
    }
}


