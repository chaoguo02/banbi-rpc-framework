package com.banbi.rpc.codec;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.enumeration.PackageType;
import com.banbi.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);
    private static final int MAGIC_NUMBER =  0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer){
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER);
        if(msg instanceof RpcRequest){
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        }else{
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        out.writeInt(serializer.getCode());
        logger.info("encode的serializer：{}", serializer.getCode());
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
