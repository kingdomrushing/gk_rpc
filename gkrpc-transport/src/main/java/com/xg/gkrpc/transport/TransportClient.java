package com.xg.gkrpc.transport;

import com.xg.gkrpc.proto.Peer;

import java.io.InputStream;

/**
 * 客户端网络通信模块
 * 1. 建立连接
 * 2. 发送数据，并且等待相应
 * 3. 关闭连接
 */
public interface TransportClient {

    void connect(Peer peer);

    InputStream write(InputStream data);

    void close();
}
