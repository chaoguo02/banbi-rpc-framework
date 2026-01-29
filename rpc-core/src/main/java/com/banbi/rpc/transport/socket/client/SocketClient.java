package com.banbi.rpc.transport.socket.client;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.loadbalancer.LoadBalancer;
import com.banbi.rpc.loadbalancer.RandomLoadBalancer;
import com.banbi.rpc.register.NacosServiceDiscovery;
import com.banbi.rpc.register.ServiceDiscovery;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.transport.RpcClient;
import com.banbi.rpc.transport.socket.util.ObjectReader;
import com.banbi.rpc.transport.socket.util.ObjectWriter;
import com.banbi.rpc.util.RpcMessageChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final CommonSerializer serializer;

    private final ServiceDiscovery serviceDiscovery;

    public SocketClient(){
        this(DEFAULT_SERIALIZER);
    }

    public SocketClient(Integer serializerCode){
        this(serializerCode, new RandomLoadBalancer());
    }

    public SocketClient(LoadBalancer loadBalancer){
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    public SocketClient(Integer serializerCode, LoadBalancer loadBalancer){
        serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        serializer = CommonSerializer.getByCode(serializerCode);
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
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());

        // 建立对服务端的连接（短连接）
        try (Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            ObjectWriter.writeObject(os, rpcRequest, serializer);
            Object obj = ObjectReader.readObject(is);
            // 阻塞等待并读取客户端响应
            RpcResponse rpcResponse = (RpcResponse) obj;
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            // 返回真正的业务数据
            return rpcResponse;
        }catch (IOException e){
            logger.error("调用时有错误发生：" + e);
            throw new RpcException("服务调用失败：", e);
        }
    }


}


