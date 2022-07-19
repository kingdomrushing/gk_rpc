package com.xg.gkrpc.proto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表示网络传输的一个端点
 */
@Data
@AllArgsConstructor
public class Peer {

    private String host;

    private Integer port;
}
