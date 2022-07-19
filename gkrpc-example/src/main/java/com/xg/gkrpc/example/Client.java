package com.xg.gkrpc.example;

import com.xg.gkrpc.client.RPCClient;

public class Client {
    public static void main(String[] args) {
        RPCClient client = new RPCClient();
        CalcService proxy = client.getProxy(CalcService.class);
        System.out.println("add method");
        int add = proxy.add(1, 2);

        System.out.println("minus method");
        int minus = proxy.minus(1, 2);
        System.out.println(add + " , " + minus);
    }
}
