package com.xg.gkrpc.server;

import com.xg.gkrpc.codec.Decoder;
import com.xg.gkrpc.codec.Encoder;
import com.xg.gkrpc.common.ReflectionUtils;
import com.xg.gkrpc.proto.Request;
import com.xg.gkrpc.proto.Response;
import com.xg.gkrpc.transport.RequestHandler;
import com.xg.gkrpc.transport.TransportServer;
import lombok.extern.slf4j.Slf4j;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class RPCServer {

    /**
     * 服务配置信息
     */
    private RPCServerConfig config;

    private TransportServer net;

    private Encoder encoder;

    private Decoder decoder;

    private ServiceManager serviceManager;

    private RequestHandler handler = new RequestHandler() {
        @Override
        public void onRequest(InputStream inputStream, OutputStream outputStream) {
            Response response = new Response();
            try {
                // 处理请求
                byte[] bytes = IOUtils.readFully(inputStream, inputStream.available(), true);
                Request request = decoder.decode(bytes, Request.class);
                log.info("get request: {}", request.toString());
                ServiceInstance serviceInstance = serviceManager.lookup(request);
                if (serviceInstance == null) {
                    throw new IllegalStateException("service not exist!");
                }
                Object res = ServiceInvoke.invoke(serviceInstance, request);
                response.setData(res);

            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                response.setCode(400);
                response.setMessage("RpcServer get Error: " + e.getClass().getName() + "\n :" + e.getMessage());
            } finally {
                byte[] bytes = encoder.encode(response);
                try {
                    outputStream.write(bytes);
                    log.info("response to client!");
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    };

    public RPCServer() {
        this(new RPCServerConfig());
    }

    public RPCServer(RPCServerConfig rpcServerConfig) {
        this.config = rpcServerConfig;

        this.encoder = ReflectionUtils.newInstance(config.getEncoder());
        this.decoder = ReflectionUtils.newInstance(config.getDecoder());
        this.net = ReflectionUtils.newInstance(config.getTransportClass());
        this.net.init(config.getPort(), this.handler);

        this.serviceManager = new ServiceManager();
    }

    public <T> void register(Class<T> interfaceClass, T bean) {
        serviceManager.register(interfaceClass, bean);
    }

    public void start() {
        this.net.start();
    }

    public void stop() {
        this.net.stop();
    }

}
