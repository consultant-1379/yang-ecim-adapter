/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class DiscardChanges extends Rpc{
    private static final Logger logger = LoggerFactory.getLogger(EditConfig.class);



    protected DiscardChanges(final Rpc rpc) {
	super(rpc);
    }

    @Override
    public DiscardChanges processTag(final String tag, final Attributes attributes, final int level) {

	return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
	final String characters = new StringBuffer().append(ch, start, length).toString();
	logger.trace("Characters: {}", characters);
	return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
	return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
	commandListener.commit(messageId, out);
	return new Nop();
    }
}
