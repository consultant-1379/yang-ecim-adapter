/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.xml.sax.Attributes;

import java.io.PrintWriter;

public class Nop implements Operation {

    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        if (level == 0) {
            return new Request(tag).processTag(tag, attributes, level);
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        throw new UnsupportedOperationException("I cannot invoke an empty operation");
    }

    @Override
    public Operation sendError(final CommandListener commandListener, final String errorMessage, final PrintWriter out) {
        throw new UnsupportedOperationException("I cannot invoke an empty operation");
    }
}
