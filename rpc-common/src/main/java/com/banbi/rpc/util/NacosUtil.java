package com.banbi.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

public class NacosUtil {

    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    private static final String SERVER_ADDR = "127.0.0.1:8849";

    public static NamingService getNacosNamingService(){
        try{
            Properties properties = new Properties();
            properties.put("serverAddr", SERVER_ADDR);
            properties.put("username", "nacos");
            properties.put("password", "nacos");
            return NamingFactory.createNamingService(properties);
        }catch (NacosException e){
            logger.error("连接到Nacos时有错误发生:", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    public static void registerService(NamingService namingService, String serviceName, InetSocketAddress inetSocketAddress) throws NacosException{
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }

    public static List<Instance> getAllInstance(NamingService namingService, String serviceName) throws NacosException{
        return namingService.getAllInstances(serviceName);
    }
}
