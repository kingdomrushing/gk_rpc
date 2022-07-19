package com.xg.gkrpc.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    /**
     * 根据class创建对象
     * @param clazz 待创建对象的类
     * @param <T> 对象类型
     * @return 创建好的对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取某个class的公有方法
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Method[] getPublicMethods(Class<T> clazz) {
        // 类自身所有的方法，包括私有，公共等，不包含父类的
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> pMethods = new ArrayList<>();
        for (Method declaredMethod : declaredMethods) {
            if (Modifier.isPublic(declaredMethod.getModifiers())) {
                pMethods.add(declaredMethod);
            }
        }
        return pMethods.toArray(new Method[0]);
    }

    /**
     * 调用指定对象的指定方法，若是静态方法，则obj为NULL
     * @param obj 指定对象
     * @param method 指定方法
     * @param args 参数
     * @return 方法结果
     */
    public static Object invoke(Object obj,
                                Method method,
                                Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
