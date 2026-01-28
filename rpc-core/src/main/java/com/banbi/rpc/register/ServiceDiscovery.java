package com.banbi.rpc.register;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {

    InetSocketAddress lookupService(String serviceName);
}
