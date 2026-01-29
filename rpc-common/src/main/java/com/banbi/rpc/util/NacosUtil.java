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
import java.util.*;

public class NacosUtil {

    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    /*
    NamingService是Nacos Client用来左服务注册/发现的核心接口
     */
    private static final NamingService namingService;
    // 用于保存当前进程曾经注册过哪些服务名
    private static final Set<String> serviceNames = new HashSet<>();

    private static InetSocketAddress address;

    static {
        namingService = getNacosNamingService();
    }

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

    /*
        调用registerInstance将服务注册到Nacos
     */
    public static void registerService(String serviceName, InetSocketAddress inetSocketAddress) throws NacosException{
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        NacosUtil.address = inetSocketAddress;
        serviceNames.add(serviceName);
    }

    /*
        服务发现
     */
    public static List<Instance> getAllInstance(String serviceName) throws NacosException{
        return namingService.getAllInstances(serviceName);
    }

    /*
        批量注销当前进程注册过的服务
     */
    public static void clearRegistry(){
        if(!serviceNames.isEmpty() && address != null){
            String host = address.getHostName();
            int port = address.getPort();
            Iterator<String> iterator = serviceNames.iterator();
            while(iterator.hasNext()){
                String serviceName = iterator.next();
                try{
                    namingService.deregisterInstance(serviceName, host, port);
                }catch (NacosException e){
                    logger.error("注销服务{}失败", serviceName, e);
                }
            }
        }
    }
}
