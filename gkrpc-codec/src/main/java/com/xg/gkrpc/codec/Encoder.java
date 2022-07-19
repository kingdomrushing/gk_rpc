package com.xg.gkrpc.codec;

/**
 * 序列化
 */
public interface Encoder {
    byte[] encode(Object obj);
}
