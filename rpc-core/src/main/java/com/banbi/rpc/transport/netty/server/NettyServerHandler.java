package com.banbi.rpc.transport.netty.server;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.entity.RpcResponse;
import com.banbi.rpc.handler.RequestHandler;
import com.banbi.rpc.registry.DefaultServiceRegistry;
import com.banbi.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty服务端入站Rpc请求的业务处理器
 * 收到RpcRequest-》从注册中心找到服务发现-》反射调用目标方法-》 封装RpcResponse写回客户端-》关闭连接
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private static ServiceRegistry serviceRegistry;

    static{
        requestHandler = new RequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            logger.info("服务端接收到请求：{}", msg);
            // 取接口名
            String interfaceName = msg.getInterfaceName();
            // 从注册中心拿到服务实现对象
            Object service = serviceRegistry.getService(interfaceName);
            // 执行方法调用，并返回结果
            Object response = requestHandler.handle(msg, service);
            // 封装响应结果为RpcResponse.success()
            ChannelFuture future = ctx.writeAndFlush(response);
            // 写完断开连接
            future.addListener(ChannelFutureListener.CLOSE);
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生");
        cause.printStackTrace();
        ctx.close();
    }
}
