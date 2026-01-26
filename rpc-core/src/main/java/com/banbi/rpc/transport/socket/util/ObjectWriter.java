package com.banbi.rpc.transport.socket.util;

import com.banbi.rpc.entity.RpcRequest;
import com.banbi.rpc.enumeration.PackageType;
import com.banbi.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class ObjectWriter {

    private static final Logger logger = LoggerFactory.getLogger(ObjectWriter.class);

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void writeObject(OutputStream out, Object obj, CommonSerializer serializer) throws IOException{
        out.write(intToBytes(MAGIC_NUMBER));
        if(obj instanceof RpcRequest){
            out.write(intToBytes(PackageType.REQUEST_PACK.getCode()));
        }else{
            out.write(intToBytes(PackageType.RESPONSE_PACK.getCode()));
        }
        out.write(intToBytes(serializer.getCode()));
        byte[] bytes = serializer.serialize(obj);
        out.write(intToBytes(bytes.length));
        out.write(bytes);
        out.flush();
    }


    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value>>24) & 0xFF);
        src[1] = (byte) ((value>>16) & 0xFF);
        src[2] = (byte) ((value>>8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
}
