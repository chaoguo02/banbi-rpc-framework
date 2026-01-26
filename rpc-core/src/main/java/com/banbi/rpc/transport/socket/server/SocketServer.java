package com.banbi.rpc.transport.socket.server;


import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.registry.ServiceRegistry;
import com.banbi.rpc.handler.RequestHandler;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * RPC 服务端的 Socket 监听与请求分发模块
 */
public class SocketServer implements RpcServer {

    private final ExecutorService threadPool;
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUN_POOL_SIZE = 50;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private final ServiceRegistry serviceRegistry;

    private CommonSerializer serializer;

    private RequestHandler requestHandler = new RequestHandler();

    public SocketServer(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUN_POOL_SIZE,KEEP_ALIVE_TIME,TimeUnit.SECONDS,workingQueue,threadFactory);
    }

    /**
     * 启动服务端监听端口，循环接受客户端连接，并把连接交给线程池处理
     * @param port
     */
    public void start(int port){
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // 创建serversocket并监听端口
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务器启动...");
            Socket socket;
            // 循环阻塞等待客户端连接
            while((socket = serverSocket.accept()) != null){
                logger.info("客户端连接！ {}:{}", socket.getInetAddress(), socket.getPort());
                // 把连接交给线程池异常处理
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry, serializer));
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
