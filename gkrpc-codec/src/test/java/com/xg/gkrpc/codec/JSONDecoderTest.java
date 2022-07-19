package com.xg.gkrpc.codec;

import junit.framework.TestCase;

public class JSONDecoderTest extends TestCase {

    public void testDecode() {
        TestBean bean = new TestBean("Marry", 18);
        Encoder encoder = new JSONEncoder();
        byte[] bytes = encoder.encode(bean);

        Decoder decoder = new JSONDecoder();
        TestBean decode = decoder.decode(bytes, TestBean.class);
        assertEquals(bean.getName(), decode.getName());
        assertEquals(bean.getAge(), decode.getAge());
    }
}