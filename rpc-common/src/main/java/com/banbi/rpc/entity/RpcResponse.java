package com.banbi.rpc.entity;

import lombok.Data;
import java.io.Serializable;
import com.banbi.rpc.enumeration.ResponseCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    // 响应状态码
    private Integer statusCode;
    // 响应状态码对应的信息
    private String messsage;
    // 成功时的响应数据
    private T data;

    /*
        响应成功时服务端返回的对象
     */

    public static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /*
        响应失败时服务端返回的对象
     */
    public static <T> RpcResponse<T> fail(ResponseCode code){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMesssage(code.getMessage());
        return response;
    }

}
