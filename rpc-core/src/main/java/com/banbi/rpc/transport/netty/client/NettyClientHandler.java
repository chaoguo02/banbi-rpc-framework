package com.banbi.rpc.transport.netty.client;

import com.banbi.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /*
    当pipeline前面的Decoder把字节流解码成RpcResponse之后，才会进入channelRead0
    该Handler的逻辑：
        1.收到RpcResponse
        2.把response保存到channel.attr
        3.关闭channel，让sendRequest结束等待
    如果解码失败，就会走exceptionCaught
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        try{
            logger.info(String.format("客户端接收到消息：%s", msg));
            // 创建/获取一个名为rpcResponse的Channel属性key
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + msg.getRequestId());
            ctx.channel().attr(key).set(msg);
            ctx.channel().close();
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("过程调用中有错误发生");
        cause.printStackTrace();
        ctx.close();
    }
}
