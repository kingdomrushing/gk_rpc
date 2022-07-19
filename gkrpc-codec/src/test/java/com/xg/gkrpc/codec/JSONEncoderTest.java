package com.xg.gkrpc.codec;

import junit.framework.TestCase;

public class JSONEncoderTest extends TestCase {

    public void testEncode() {
        TestBean bean = new TestBean("Marry", 18);
        Encoder encoder = new JSONEncoder();
        byte[] encode = encoder.encode(bean);

        assertNotNull(encode);
    }
}