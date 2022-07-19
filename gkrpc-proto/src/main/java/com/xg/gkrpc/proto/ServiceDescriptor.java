package com.xg.gkrpc.proto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 表示服务
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDescriptor {

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 参数类型
     */
    private String[] parameterTypes;

    /**
     * 根据类和方法获取serviceDescriptor
     * @param clazz
     * @param method
     * @return
     */
    public static ServiceDescriptor getServiceDescriptor(Class<?> clazz, Method method) {
        ServiceDescriptor sdp = new ServiceDescriptor();
        sdp.setClassName(clazz.getName());
        sdp.setMethodName(method.getName());
        sdp.setReturnType(method.getReturnType().getName());

        Class<?>[] types = method.getParameterTypes();
        String[] parameterTypes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            parameterTypes[i] = types[i].getName();
        }
        sdp.setParameterTypes(parameterTypes);

        return sdp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDescriptor that = (ServiceDescriptor) o;
        return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return "ServiceDescriptor{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }
}
