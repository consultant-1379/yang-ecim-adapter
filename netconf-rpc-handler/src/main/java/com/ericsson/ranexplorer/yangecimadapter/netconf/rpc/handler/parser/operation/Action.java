/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.xml.sax.Attributes;

public class Action extends Rpc {

    private final StringBuilder actionMessage = new StringBuilder();
    private int tagLevel = 0;

    protected Action(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Action processTag(final String tag, final Attributes attributes, final int level) {
        if (level > 2) {
            actionMessage.append("<").append(tag);
            for (int i = 0; i < attributes.getLength(); i++) {
                actionMessage.append(" ").append(attributes.getLocalName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
            }
            actionMessage.append(">");
            tagLevel++;
        }
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        if (tagLevel>0 && length>0 ) {
            actionMessage.append(String.valueOf(ch).substring(start, start+length));
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        if (level > 2) {
            actionMessage.append("</").append(tag).append(">");
            tagLevel--;
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.action(messageId, actionMessage.toString(), out);
        return new Nop();
    }
}
