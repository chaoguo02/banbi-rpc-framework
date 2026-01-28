package com.banbi.rpc.register;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    /*
        将一个服务注册到注册表
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

}
