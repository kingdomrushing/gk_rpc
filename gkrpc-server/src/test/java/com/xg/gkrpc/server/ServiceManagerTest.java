package com.xg.gkrpc.server;

import com.xg.gkrpc.common.ReflectionUtils;
import com.xg.gkrpc.proto.Request;
import com.xg.gkrpc.proto.ServiceDescriptor;
import junit.framework.TestCase;

import java.lang.reflect.Method;

public class ServiceManagerTest extends TestCase {


    public void testRegister() {
        ServiceManager sm = new ServiceManager();

        TestInterface bean = new TestImpl();
        sm.register(TestInterface.class, bean);
    }

    public void testLookup() {
        Method method = ReflectionUtils.getPublicMethods(TestInterface.class)[0];
        ServiceDescriptor sd = ServiceDescriptor.getServiceDescriptor(TestInterface.class, method);
        Request request = new Request();
        request.setServiceDescriptor(sd);

        ServiceManager sm = new ServiceManager();
        ServiceInstance instance = sm.lookup(request);
        assertNull(instance);

        TestInterface bean = new TestImpl();
        sm.register(TestInterface.class, bean);
        ServiceInstance instance1 = sm.lookup(request);
        assertNotNull(instance1);
    }
}