package com.xg.gkrpc.client;

import com.xg.gkrpc.codec.Decoder;
import com.xg.gkrpc.codec.Encoder;
import com.xg.gkrpc.common.ReflectionUtils;

import java.lang.reflect.Proxy;

public class RPCClient {

    private RPCClientConfig config;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public RPCClient() {
        this(new RPCClientConfig());
    }

    public RPCClient(RPCClientConfig config) {
        this.config = config;

        this.encoder = ReflectionUtils.newInstance(config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(config.getDecoderClass());
        this.selector = ReflectionUtils.newInstance(config.getSelectorClass());
        this.selector.init(config.getServers(), config.getConnectCount(), config.getTransportClass());
    }

    /**
     * 创建代理对象,由代理对象去发送请求
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new RemoteHandler(clazz, encoder, decoder, selector));
    }
}
