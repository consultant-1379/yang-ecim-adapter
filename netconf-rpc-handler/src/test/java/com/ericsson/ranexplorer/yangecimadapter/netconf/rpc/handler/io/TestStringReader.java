/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestStringReader {

    protected final NetconfInputStream io;
    protected final List<Byte> bs;

    public TestStringReader(String toRead){
        this.io = new NetconfInputStream(new ByteArrayInputStream(toRead.getBytes(StandardCharsets.UTF_8)));
        this.bs = new ArrayList<>();
    }

    public TestStringReader readBytes(String sample) throws IOException {
        byte[] bytes = new byte[sample.getBytes(StandardCharsets.UTF_8).length];
        toList(bytes, io.read(bytes));
        return this;
    }

    public String readAll() throws IOException {
        byte b;
        while ((b = (byte) io.read()) != -1) {
            bs.add(b);
        }
        this.close();
        return new String(toArray(), StandardCharsets.UTF_8);
    }

    private void toList(byte[] bytes, int read) {
        for (int i = 0; i < read; i++){
            bs.add(bytes[i]);
        }
    }

    private byte[] toArray() {
        byte[] result = new byte[bs.size()];
        Iterator<Byte> it = bs.iterator();
        for (int i = 0; i < result.length; i++){
            result[i] = it.next();
        }
        return result;
    }

    public String get() throws IOException {
        this.close();
        return new String(toArray(), StandardCharsets.UTF_8);
    }

    public String current() throws IOException {
        return new String(toArray(), StandardCharsets.UTF_8);
    }

    public TestStringReader close() throws IOException {
        io.close();
        return this;
    }

    public TestStringReader read() throws IOException {
        int b = io.read();
        if (b != -1){
            bs.add((byte) b);
        }
        return this;
    }

    public TestStringReader reportError() {
        this.io.error();
        return this;
    }
}
