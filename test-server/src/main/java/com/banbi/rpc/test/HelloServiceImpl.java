package com.banbi.rpc.test;

import com.banbi.rpc.annotation.Service;
import com.banbi.rpc.api.HelloObject;
import com.banbi.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject helloObject) {
        logger.info("接收到消息：{}", helloObject.getMessage());
        return "这是调用的返回值： id=" + helloObject.getId() + "\nmsg：" + helloObject.getMessage();
    }
}
