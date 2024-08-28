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

public class CustomOperation extends Rpc {
    private static final Logger logger = LoggerFactory.getLogger(CustomOperation.class);
    StringBuffer requestBody = new StringBuffer();
    private String rpcError = "";
    boolean isOperationSet = false;

    protected CustomOperation(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        if (level >= 0) {
            if (!isOperationSet) {
                requestBody.append("<invoke-cli xmlns=\"urn:com:ericsson:nem-cli\">");
                isOperationSet = true;
            }
            logger.trace(" Level {}, tag: {}", level, tag.toLowerCase());
            requestBody.append("<").append(tag);
            for (int i = 0; i < attributes.getLength(); i++) {
                logger.trace("Attribute {}: {}", attributes.getLocalName(i), attributes.getValue(i));
                if ("operation".equals(attributes.getLocalName(i))) {
                    try {
                        com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.api.Operation.valueOf(attributes.getValue(i).trim().toUpperCase());
                    } catch (final IllegalArgumentException exception) {
                        logger.error("Unknown operation: {}", attributes.getValue(i), exception.getMessage(), exception);
                        rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
                    }
                }
                requestBody.append(" ").append(attributes.getLocalName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
            }
            requestBody.append(">");
            logger.info("Request body in custom operation is {} ", requestBody);
        }
        return this;

    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        if (!"rpc".equals(tag) && level >= 0 && requestBody != null) {
            requestBody.append("</").append(tag).append(">");
            logger.trace("5) level {}, tag: {} ", level, tag.toLowerCase());
        }
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        final String characters = new StringBuffer().append(ch, start, length).toString();
        logger.trace("Characters: {}", characters);
        if (requestBody != null) {
            requestBody.append(characters);
            logger.info("data from the xml file is {} ", requestBody);
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        if (rpcError.isEmpty()) {
            commandListener.customOperation(messageId, requestBody.toString(), out);
        } else {
            commandListener.sendError(messageId, rpcError, out);
        }
        return new Nop();
    }

    @Override
    public Operation sendError(final CommandListener commandListener, final String errorMessage, final PrintWriter out) {
        return this;
    }

    @Override
    public String toString() {
        return "CustomOperation";
    }

}
