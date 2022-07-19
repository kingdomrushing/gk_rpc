package com.xg.gkrpc.transport;

/**
 * 服务端网络通信模块
 * 1. 启动、监听
 * 2. 接受请求
 * 3. 关闭监听
 */
public interface TransportServer {

    /**
     * 初始化时将handler传进来
     * @param port
     * @param handler
     */
    void init(int port, RequestHandler handler);

    void start();

    void stop();
}
