package com.banbi.rpc.test;

import com.banbi.rpc.annotation.Service;
import com.banbi.rpc.api.ByeService;

@Service
public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye," + name;
    }
}
