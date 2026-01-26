package com.banbi.rpc.test;

import com.banbi.rpc.api.ByeService;

public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye," + name;
    }
}
