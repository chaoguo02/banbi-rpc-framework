package com.banbi.rpc.transport.netty.server;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.handler.RequestHandler;
import com.banbi.rpc.util.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Netty服务端入站Rpc请求的业务处理器
 * 收到RpcRequest-》从注册中心找到服务发现-》反射调用目标方法-》 封装RpcResponse写回客户端-》关闭连接
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;

    private static final String THREAD_NAME_PREFIX = "netty-server-handler";

    private static final ExecutorService threadPool;

    static{
        requestHandler = new RequestHandler();
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        threadPool.execute(() ->{
            try{
                logger.info("服务端接收到请求：{}", msg);
                Object response = requestHandler.handle(msg);
                ChannelFuture future = ctx.writeAndFlush(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生");
        cause.printStackTrace();
        ctx.close();
    }
}
