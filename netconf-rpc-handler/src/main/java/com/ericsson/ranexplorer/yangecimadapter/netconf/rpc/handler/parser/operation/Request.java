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

public class Request implements Operation {

    protected final String name;

    protected Request(final Request request) {
	this.name = request.name;
    }

    protected Request(final String name) {
	this.name = name;
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
	if (level == 0) {
	    switch (tag) {
	    case "rpc":
		return new Rpc(this).processTag(tag, attributes, level);
	    case "hello":
		return new Hello(this).processTag(tag, attributes, level);
	    }
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
	throw new UnsupportedOperationException("I cannot invoke an unknown operation [" + this.name + "]");
    }

    @Override
    public Operation sendError(final CommandListener commandListener, final String errorMessage, final PrintWriter out) {
	throw new UnsupportedOperationException("I cannot invoke an unknown operation [" + this.name + "]");
    }
}
