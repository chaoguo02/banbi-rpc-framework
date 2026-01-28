package com.banbi.rpc.transport.netty.server;

import com.banbi.rpc.codec.CommonDecoder;
import com.banbi.rpc.codec.CommonEncoder;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.provider.ServiceProvider;
import com.banbi.rpc.provider.ServiceProviderImpl;
import com.banbi.rpc.register.NacosServiceRegistry;
import com.banbi.rpc.register.ServiceRegistry;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.banbi.rpc.serializer.JsonSerializer;

import java.net.InetSocketAddress;


public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final String host;

    private final int port;

    private final ServiceRegistry serviceRegistry;

    private final ServiceProvider serviceProvider;

    private CommonSerializer serializer;

    public NettyServer(String host, int port){
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
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
     * Netty服务端启动器：负责启动一个Netty服务器监听指定端口、接收客户端连接
     * 并通过pipeline的编解码器把网络字节流转成RpcRequest/RpcResponse，最走过交给handler执行业务
     * 并返回结果
     */
    @Override
    public void start() {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }

        /*
            两个线程组：bossGroup和workerGroup
            bossGroup：专门负责accept新连接
            workerGroup：负责已建立连接的I/O读写和pipeline中handler的执行
        */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //配置并启动服务端：用来配置channel、socket、pipeline
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    // 使用Java NIO的服务端通道来监听端口
                    .channel(NioServerSocketChannel.class)
                    // 给服务端监听通道加一个日志handler
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // option：配置监听socket，连接队列大小
                    .option(ChannelOption.SO_BACKLOG, 256)
                    // childOption：配置每个客户端连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 给每个客户端连接初始化pipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        /*
                        handler：业务处理器
                            收到RpcRequest后，找到服务实现
                            反射调用方法
                            封装RpcResponse
                            writeAndFlush写回客户端
                         */
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            // 绑定端口并阻塞等待绑定成功
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
//            阻塞等待服务端channel关闭
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时由错误发生", e);
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
