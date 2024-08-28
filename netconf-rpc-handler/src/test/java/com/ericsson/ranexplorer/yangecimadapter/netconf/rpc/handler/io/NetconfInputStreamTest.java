/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.io;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class NetconfInputStreamTest {

    @Test
    public void test1() throws IOException {
        assertEquals("test]]", from("test]]").readAll());
    }

    @Test
    public void test2() throws IOException {
        assertEquals("test]]>]]>", from("test]]>]]>").readAll());
    }

    @Test
    public void test3() throws IOException {
        assertEquals("test]]>]]>next", from("test]]>]]>next").readBytes("test]]>]]>next").readBytes("next").get());
    }

    @Test
    public void test4() throws IOException {
        assertEquals("test]]>]]>next", from("test]]>]]>next").readBytes("test]]>").readBytes("]]>next").
                readBytes("next").get());
    }

    @Test
    public void test5() throws IOException {
        assertEquals("test]]>]]>next", from("test]]>]]>next").readBytes("test]]>").readBytes("]]>next").
                read().readBytes("]>next").get());
    }

    @Test
    public void test6() throws IOException {
        assertEquals("test\n]]>]]>\n<next", from("test\n]]>]]>\n<next").readBytes("test\n]]>").readBytes("]]>\n<next").
                readBytes("\n<next").get());
    }

    @Test
    public void test7() throws IOException {
        assertEquals("test\n]]>]]><next", from("test\n]]>]]>\n<next").readBytes("test\n]]>").readBytes("]]>\n<next").
                reportError().readBytes("\n<next").readBytes("<next").get());
    }

    private TestStringReader from(String toRead) {
        return new TestStringReader(toRead);
    }

}
