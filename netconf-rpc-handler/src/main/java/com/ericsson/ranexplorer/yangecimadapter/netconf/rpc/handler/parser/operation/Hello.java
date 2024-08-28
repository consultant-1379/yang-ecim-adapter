/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Hello extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(Hello.class);

    protected List<String> capabilities;
    private StringBuilder buffer;

    Hello(final Request request) {
        super(request);
    }

    @Override
    public Hello processTag(final String tag, final Attributes attributes, final int level) {
        LOG.trace("Processing capabilities element node");
        if("capabilities".equals(tag) && level == 1) {
                this.capabilities = new LinkedList<>();
        }
        else if("capability".equals(tag) && level == 2) {
             buffer = new StringBuilder();
        }
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        if (buffer != null) {
            buffer.append(ch, start, length);
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        if ("capability".equalsIgnoreCase(tag) && level == 2 && this.capabilities != null) {
            this.capabilities.add(buffer.toString());
            buffer = null;
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        LOG.trace("Invoke method called in Hello class");
        commandListener.clientHello(
                this.capabilities == null ? Collections.<String> emptyList() : Collections
                        .unmodifiableList(this.capabilities), out);
        return new Nop();
    }

}
