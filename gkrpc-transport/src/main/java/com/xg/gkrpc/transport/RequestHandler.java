package com.xg.gkrpc.transport;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 处理网络请求的handler
 */
public interface RequestHandler {

    void onRequest(InputStream receive, OutputStream response);
}
