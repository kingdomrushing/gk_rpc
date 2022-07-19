package com.xg.gkrpc.server;

public class TestImpl implements TestInterface{
    @Override
    public void sayHello() {
        System.out.println("hello");
    }
}
