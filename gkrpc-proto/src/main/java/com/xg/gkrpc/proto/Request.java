package com.xg.gkrpc.proto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示RPC的一个请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    /**
     * 表示请求的服务
     */
    private ServiceDescriptor serviceDescriptor;

    /**
     * 请求携带的参数
     */
    private Object[] parameters;
}
