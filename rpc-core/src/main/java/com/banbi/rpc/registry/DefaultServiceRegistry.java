package com.banbi.rpc.registry;

import com.banbi.rpc.enumeration.RpcError;
import com.banbi.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);
    // key:服务名称（接口名），value：服务实体（实体类的实例对象）
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    // 存储实现类的名称，比存放接口名称占用的空间更小
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 把一个服务实现对象注册到注册中心里，并建立“接口名 → 实现对象”的映射
     * @param service 真正的服务对象
     * @param <T>
     */
    @Override
    public <T> void register(T service) {
        // 拿到实现类名称
        String serviceImplName = service.getClass().getCanonicalName();
        // 去重：同一实现类只注册一次
        if(registeredService.contains(serviceImplName)){
            return;
        }
        registeredService.add(serviceImplName);
        // 获取该实现类可能实现的多个服务接口，使用Class数组接收
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0){
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        // 逐个接口注册：接口名 → 实现对象
        for(Class<?> i: interfaces){
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口：{}注册服务：{}", interfaces, serviceImplName);
    }

    /**
     * 根据服务名（接口全限定名）获取对应的服务实现对象。
     * @param serviceName
     * @return
     */
    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(service == null){
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
