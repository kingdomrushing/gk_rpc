package com.xg.gkrpc.server;

import com.xg.gkrpc.common.ReflectionUtils;
import com.xg.gkrpc.proto.Request;

/**
 * 通过反射执行服务实例的方法
 */
public class ServiceInvoke {

    public static Object invoke(ServiceInstance serviceInstance, Request request) {
        return ReflectionUtils.invoke(
                serviceInstance.getTarget(),
                serviceInstance.getMethod(),
                request.getParameters());
    }
}
