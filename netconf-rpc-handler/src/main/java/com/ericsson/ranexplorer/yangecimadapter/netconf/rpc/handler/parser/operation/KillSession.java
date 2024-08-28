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

public class KillSession extends Rpc {

    private static final Logger logger = LoggerFactory.getLogger(KillSession.class);

    private int sessionId;
    private StringBuilder buffer;

    public KillSession(final Rpc rpc) {
        super(rpc);
        this.sessionId = -1;
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        if ("session-id".equalsIgnoreCase(tag) && level == 2) {
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
        if ("session-id".equalsIgnoreCase(tag) && level == 2) {
            try {
                this.sessionId = Integer.valueOf(buffer.toString());
            } catch (NumberFormatException e) {
                logger.warn("Wrong format of session-id {}", buffer);
            }
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.killSession(messageId, sessionId, null, out);
        return new Nop();
    }
}
