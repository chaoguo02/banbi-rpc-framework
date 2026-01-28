package com.banbi.rpc.transport.socket.server;


import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.hook.ShutdownHook;
import com.banbi.rpc.provider.ServiceProvider;
import com.banbi.rpc.provider.ServiceProviderImpl;
import com.banbi.rpc.register.NacosServiceRegistry;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.handler.RequestHandler;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.transport.RpcServer;
import com.banbi.rpc.factory.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * RPC 服务端的 Socket 监听与请求分发模块
 */
public class SocketServer implements RpcServer {

    private final ExecutorService threadPool;

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private final String host;

    private final int port;

    private CommonSerializer serializer;

    private RequestHandler requestHandler = new RequestHandler();

    private final ServiceRegistry serviceRegistry;

    private final ServiceProvider serviceProvider;

    public SocketServer(String host, int port){
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
    }

    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    /**
     * 启动服务端监听端口，循环接受客户端连接，并把连接交给线程池处理
     */
    public void start(){
        // 创建serversocket并监听端口
        try(ServerSocket serverSocket = new ServerSocket()){
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动...");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            // 循环阻塞等待客户端连接
            while((socket = serverSocket.accept()) != null){
                logger.info("客户端连接！ {}:{}", socket.getInetAddress(), socket.getPort());
                // 把连接交给线程池异常处理
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        }catch(IOException e){
            logger.error("服务器启动时有错误发生：", e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
