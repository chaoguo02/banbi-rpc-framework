package com.banbi.rpc.transport.socket.client;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.ResponseCode;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.transport.RpcClient;
import com.banbi.rpc.transport.socket.util.ObjectReader;
import com.banbi.rpc.transport.socket.util.ObjectWriter;
import com.banbi.rpc.util.RpcManagerChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

@AllArgsConstructor
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;

    private final int port;

    private CommonSerializer serializer;

    public SocketClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * 在 RPC 客户端侧发起一次远程调用
     * @param rpcRequest
     * @return
     */
    public Object sendRequest(RpcRequest rpcRequest){
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // 建立对服务端的连接（短连接）
        try (Socket socket = new Socket(host, port)){
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            ObjectWriter.writeObject(os, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(is);
            // 阻塞等待并读取客户端响应
            RpcResponse rpcResponse = (RpcResponse) obj;
            RpcManagerChecker.check(rpcRequest, rpcResponse);
            // 返回真正的业务数据
            return rpcResponse.getData();
        }catch (IOException e){
            logger.error("调用时有错误发生：" + e);
            throw new RpcException("服务调用失败：", e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}


