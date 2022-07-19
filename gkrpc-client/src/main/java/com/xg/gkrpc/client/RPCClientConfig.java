package com.xg.gkrpc.client;

import com.xg.gkrpc.codec.Decoder;
import com.xg.gkrpc.codec.Encoder;
import com.xg.gkrpc.codec.JSONDecoder;
import com.xg.gkrpc.codec.JSONEncoder;
import com.xg.gkrpc.proto.Peer;
import com.xg.gkrpc.transport.HttpTransportClient;
import com.xg.gkrpc.transport.TransportClient;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * client配置
 */
@Data
public class RPCClientConfig {

    private Class<? extends TransportClient> transportClass = HttpTransportClient.class;
    private Class<? extends Decoder> decoderClass = JSONDecoder.class;
    private Class<? extends Encoder> encoderClass = JSONEncoder.class;
    private Class<? extends TransportSelector> selectorClass = RandomTransportSelector.class;
    private Integer connectCount = 1;
    private List<Peer> servers = Arrays.asList(
            new Peer("127.0.0.1", 3000)
    );
}
