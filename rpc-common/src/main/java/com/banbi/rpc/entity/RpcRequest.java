package com.banbi.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {
    // 待调用接口名称
    private String interfaceName;
    // 待调用方法名称
    private String methodName;
    // 待调用方法参数
    private Object[] parameters;
    // 带调用方法的参数类型
    private Class<?>[] paramTypes;

}
