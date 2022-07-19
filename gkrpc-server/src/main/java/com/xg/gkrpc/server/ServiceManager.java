package com.xg.gkrpc.server;

import com.xg.gkrpc.common.ReflectionUtils;
import com.xg.gkrpc.proto.Request;
import com.xg.gkrpc.proto.ServiceDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理RPC暴露的服务
 */
@Slf4j
public class ServiceManager {
    /**
     * 保存注册的service
     */
    private Map<ServiceDescriptor, ServiceInstance> services;

    public ServiceManager() {
        this.services = new ConcurrentHashMap<>();
    }

    /**
     * 注册服务
     * @param interfaceClass
     * @param bean
     * @param <T>
     */
    public <T> void register(Class<T> interfaceClass, T bean) {
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for (Method method : methods) {
            ServiceInstance serviceInstance = new ServiceInstance(bean, method);
            ServiceDescriptor sd = ServiceDescriptor.getServiceDescriptor(interfaceClass, method);
            this.services.put(sd, serviceInstance);

            log.info("register service: {} {}", sd.getClassName(), sd.getMethodName());
        }
    }

    /**
     * 查找服务
     * @param request
     * @return
     */
    public ServiceInstance lookup(Request request) {
        return services.get(request.getServiceDescriptor());
    }
}
