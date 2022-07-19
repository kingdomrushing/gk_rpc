package com.xg.gkrpc.server;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 表示一个具体服务, 哪个对象暴露出哪个方法
 */
@AllArgsConstructor
@Data
public class ServiceInstance {

    private Object target;

    private Method method;
}
