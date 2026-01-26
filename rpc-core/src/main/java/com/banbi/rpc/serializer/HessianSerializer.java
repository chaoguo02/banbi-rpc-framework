package com.banbi.rpc.serializer;

import com.banbi.rpc.enumeration.SerializerCode;
import com.banbi.rpc.exception.SerializeException;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    public byte[] serialize(Object obj) {
        HessianOutput hessianOutput = null;
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            hessianOutput = new HessianOutput(bos);
            hessianOutput.writeObject(obj);
            return bos.toByteArray();
        }catch (IOException e){
            logger.error("序列化时有错误发生" + e);
            throw new SerializeException("序列化时有错误发生");
        }
        finally {
            if(hessianOutput != null){
                try {
                    hessianOutput.close();
                }catch (IOException e){
                    logger.error("关闭output流时有错误发生");
                }
            }
        }
    }


    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        HessianInput hessianInput = null;
        try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes)){
            hessianInput = new HessianInput(bis);
            return hessianInput.readObject();
        }catch(IOException e){
            logger.error("反序列化时有错误发生" + e);
            throw new SerializeException("反序列化时有错误发生");
        }
        finally {
            if(hessianInput != null){
                hessianInput.close();
            }
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("HESSIAN").getCode();
    }
}
