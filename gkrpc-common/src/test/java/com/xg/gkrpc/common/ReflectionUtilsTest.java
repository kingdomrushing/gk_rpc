package com.xg.gkrpc.common;

import junit.framework.TestCase;
import org.junit.Assert;

import java.lang.reflect.Method;

public class ReflectionUtilsTest extends TestCase {

    public void testNewInstance() {
        TestClass testClass = ReflectionUtils.newInstance(TestClass.class);
        Assert.assertNotNull(testClass);
    }

    public void testGetPublicMethods() {
        Method[] publicMethods = ReflectionUtils.getPublicMethods(TestClass.class);
        Assert.assertEquals(1, publicMethods.length);
        Assert.assertEquals("b", publicMethods[0].getName());
    }

    public void testInvoke() {
        Method[] publicMethods = ReflectionUtils.getPublicMethods(TestClass.class);
        Method method = publicMethods[0];
        TestClass testClass = new TestClass();
        Object o = ReflectionUtils.invoke(testClass, method);
        Assert.assertEquals("b", o);
    }
}