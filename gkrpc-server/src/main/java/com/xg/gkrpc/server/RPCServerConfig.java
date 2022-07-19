package com.xg.gkrpc.server;

import com.xg.gkrpc.codec.Decoder;
import com.xg.gkrpc.codec.Encoder;
import com.xg.gkrpc.codec.JSONDecoder;
import com.xg.gkrpc.codec.JSONEncoder;
import com.xg.gkrpc.transport.HttpTransportServer;
import com.xg.gkrpc.transport.TransportServer;
import lombok.Data;

/**
 * server配置
 */
@Data
public class RPCServerConfig {

    private Class<? extends TransportServer> transportClass = HttpTransportServer.class;
    private Class<? extends Encoder> encoder = JSONEncoder.class;
    private Class<? extends Decoder> decoder = JSONDecoder.class;

    private Integer port = 3000;
}
