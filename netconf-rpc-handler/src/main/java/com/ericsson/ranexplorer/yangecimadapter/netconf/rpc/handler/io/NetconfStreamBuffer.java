/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.io;

import java.nio.ByteBuffer;

public class NetconfStreamBuffer {

    ByteBuffer byteBuffer;

    public NetconfStreamBuffer(int initialCapacity) {
        this.byteBuffer = ByteBuffer.allocate(initialCapacity);
        this.byteBuffer.flip();
    }

    public int read(byte[] b, int off, int len) {
        if (byteBuffer.remaining() > 0) {
            int wrote = Math.min(len, byteBuffer.remaining());
            byteBuffer.get(b, off, wrote);
            return wrote;
        }
        return -1;
    }

    public void put(byte[] b, int off, int len) {
        byteBuffer.compact();
        while (len > byteBuffer.remaining()) {
            byteBuffer = byteBuffer.duplicate();
        }
        byteBuffer.put(b, off, len);
        byteBuffer.flip();
    }

    public int readSkippingLeadingSpaces(byte[] b, int off, int len) {
        while (byteBuffer.remaining() > 0) {
            byte maybeSpace = byteBuffer.get();
            if (!Character.isWhitespace((int) maybeSpace)) {
                b[off] = maybeSpace;
                return len > 1 && off + 1 < b.length ? this.read(b, off + 1, len - 1) + 1 : 1;
            }
        }
        return -1;
    }

    public int read() {
        if (byteBuffer.remaining() > 0) {
            return byteBuffer.get();
        }
        return -1;
    }

    public int readSkippingLeadingSpaces() {
        while (byteBuffer.remaining() > 0) {
            byte maybeSpace = this.byteBuffer.get();
            if (!Character.isWhitespace((int) maybeSpace)) {
                return (int) maybeSpace;
            }
        }
        return -1;
    }

}
