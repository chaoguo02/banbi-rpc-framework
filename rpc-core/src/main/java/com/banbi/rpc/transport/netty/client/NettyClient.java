package com.banbi.rpc.transport.netty.client;

import com.banbi.rpc.codec.CommonDecoder;
import com.banbi.rpc.codec.CommonEncoder;
import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import com.banbi.rpc.register.NacosServiceDiscovery;
import com.banbi.rpc.register.NacosServiceRegistry;
import com.banbi.rpc.register.ServiceDiscovery;
import com.banbi.rpc.register.ServiceRegistry;
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

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final EventLoopGroup group;

    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    private final ServiceDiscovery serviceDiscovery;

    private CommonSerializer serializer;

    public NettyClient(){
        serviceDiscovery = new NacosServiceDiscovery();
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

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 保证自定义实体类变量的原子性和共享性的线程安全，此处应用于rpcResponse
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if(!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息：%s", rpcRequest.toString()));
                } else {
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
            result.set(rpcResponse.getData());

        }catch (InterruptedException e){
            logger.error("发送消息时由错误发生：", e);
            Thread.currentThread().interrupt();
        }
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
