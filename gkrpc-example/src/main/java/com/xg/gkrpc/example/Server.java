package com.xg.gkrpc.example;

import com.xg.gkrpc.server.RPCServer;

public class Server {

    public static void main(String[] args) {
        RPCServer server = new RPCServer();
        server.register(CalcService.class, new CalcServiceImpl());
        server.start();
    }
}
