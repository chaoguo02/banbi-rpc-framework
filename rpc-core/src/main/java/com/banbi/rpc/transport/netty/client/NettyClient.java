package com.banbi.rpc.transport.netty.client;

import com.banbi.rpc.codec.CommonDecoder;
import com.banbi.rpc.codec.CommonEncoder;
import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.serializer.CommonSerializer;
import com.banbi.rpc.serializer.HessianSerializer;
import com.banbi.rpc.serializer.JsonSerializer;
import com.banbi.rpc.serializer.KryoSerializer;
import com.banbi.rpc.transport.RpcClient;
import com.banbi.rpc.util.RpcManagerChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String host;
    private int port;
    private static final Bootstrap bootstrap;

    private CommonSerializer serializer;

    public NettyClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     *   初始化Netty客户端的全局启动器Bootstrap
     *   NioEventLoopGroup：Netty的线程组
     *   Bootstrap：客户端启动器，用于connect
     *   .channel()：使用NIO的Socket通道
     *   .option()：开启TCP keepalive，保活探测
     *   .handler()：配置pipeline处理链
     *   Decoder解码器
     *   Encoder编码器
     *   handler：接收RpcResponse的业务handler
      */

    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            if(serializer == null){
                logger.error("未设置序列化器");
                throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
            }
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new CommonDecoder())
                            .addLast(new CommonEncoder(serializer))
                            .addLast(new NettyClientHandler());

                }
            });

            // 连接客户端
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务端{}：{}", host, port);
            /*
            发送请求：
                把rpcRequest写入channel
                因为pipeline有CommonEncoder，所以会自动把RpcRequest编码成字节流发出去
                listener只是打印”发送成功/失败“的日志
             */
            Channel channel = future.channel();
            if(channel != null){
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        logger.info(String.format("客户端发送消息：%s", rpcRequest.toString()));
                    }else{
                        logger.error("发送消息时有错误发生：", future1.cause());
                    }
                });
                // 等待连接关闭：阻塞等待channel被关闭，代码假设服务端处理完会关闭连接
                channel.closeFuture().sync();
                // handler收到响应后，把RpcResponse放进channel的属性attr中
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                // 返回结果
                RpcResponse rpcResponse = channel.attr(key).get();
                RpcManagerChecker.check(rpcRequest, rpcResponse);
                return rpcResponse.getData();
            }
        }catch (InterruptedException e){
            logger.error("发送消息时由错误发生：", e);
        }
        return null;
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
