package com.banbi.rpc.transport.netty.server;

import com.banbi.rpc.codec.CommonDecoder;
import com.banbi.rpc.codec.CommonEncoder;
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


public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * Netty服务端启动器：负责启动一个Netty服务器监听指定端口、接收客户端连接
     * 并通过pipeline的编解码器把网络字节流转成RpcRequest/RpcResponse，最走过交给handler执行业务
     * 并返回结果
     * @param port
     */
    @Override
    public void start(int port) {
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
                            pipeline.addLast(new CommonEncoder(new HessianSerializer()))
//                                    .addLast(new CommonEncoder(new KryoSerializer()))
//                                    .addLast(new CommonEncoder(new JsonSerializer()))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            // 绑定端口并阻塞等待绑定成功
            ChannelFuture future = serverBootstrap.bind(port).sync();
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
}
