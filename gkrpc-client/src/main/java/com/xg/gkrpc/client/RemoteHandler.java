package com.xg.gkrpc.client;

import com.xg.gkrpc.codec.Decoder;
import com.xg.gkrpc.codec.Encoder;
import com.xg.gkrpc.proto.Request;
import com.xg.gkrpc.proto.Response;
import com.xg.gkrpc.proto.ServiceDescriptor;
import com.xg.gkrpc.transport.TransportClient;
import lombok.extern.slf4j.Slf4j;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 调用远程服务的代理类
 */
@Slf4j
public class RemoteHandler implements InvocationHandler {

    private Class clazz;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public <T> RemoteHandler(Class<T> clazz, Encoder encoder, Decoder decoder, TransportSelector selector) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.selector = selector;
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setServiceDescriptor(ServiceDescriptor.getServiceDescriptor(clazz, method));
        request.setParameters(args);
        log.info("request is {}", request);

        Response response = remoteInvoke(request);
        if (response == null || response.getCode() != 0) {
            throw new IllegalStateException("fail to invoke remote: " + response);
        }

        return response.getData();
    }

    private Response remoteInvoke(Request request) {
        TransportClient client = null;
        Response decode = null;
        try {
            client = selector.select();
            byte[] encode = encoder.encode(request);
            InputStream receive = client.write(new ByteArrayInputStream(encode));
            byte[] bytes = IOUtils.readFully(receive, receive.available(), true);
            decode = decoder.decode(bytes, Response.class);

        } catch (Exception e) {
            decode = new Response();
            decode.setCode(1);
            decode.setMessage("RPCClient get Error:" + e.getClass() + "\n:" + e.getMessage());
            log.warn(e.getMessage(), e);
        } finally {
            if (client != null) {
                selector.release(client);
            }
        }
        return decode;
    }
}
