package com.banbi.rpc.test;

import com.banbi.rpc.annotation.Service;
import com.banbi.rpc.api.HelloObject;
import com.banbi.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HelloServiceImpl2 implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl2.class);

    @Override
    public String hello(HelloObject helloObject) {
        logger.info("接收到消息：{}", helloObject.getMessage());
        return "本次处理来自Socket服务";
    }
}
