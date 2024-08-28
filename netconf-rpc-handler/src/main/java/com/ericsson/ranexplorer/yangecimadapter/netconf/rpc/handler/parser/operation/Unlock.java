/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class Unlock extends Rpc {
    private static final Logger logger = LoggerFactory.getLogger(EditConfig.class);

    private boolean isTarget;
    private Datastore datastore;
    private String rpcError = "";
    private final StringBuilder requestBody = new StringBuilder();

    protected Unlock(final Rpc rpc) {
	super(rpc);
	this.isTarget = false;
	this.datastore = Datastore.RUNNING;
    }

    @Override
    public Unlock processTag(final String tag, final Attributes attributes, final int level) {
	if (level == 2 && tag.toLowerCase().equals("target")) {
	    isTarget = true;
	    logger.trace("1) Level " + level + ", tag: " + tag.toLowerCase());
	} else if (level == 3 && isTarget) {
	    logger.trace("5) Level " + level + ", tag: " + tag.toLowerCase());
	    try {
		datastore = Datastore.valueOf(tag.toUpperCase());
	    } catch (final IllegalArgumentException e) {
		logger.warn("Unknown target for datastore: {}", tag);
		rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
	    }
	}
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
	if (level == 2 && tag.toLowerCase().equals("target")) {
	    isTarget = false;
	    logger.trace("1) Level " + level + ", tag: " + tag.toLowerCase());
	} else if (level == 3 && isTarget) {
	}
	return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
	commandListener.unlock(messageId, datastore, out);
	return new Nop();
    }
}
