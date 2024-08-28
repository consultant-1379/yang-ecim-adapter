/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import org.xml.sax.Attributes;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;

public class Rpc extends Request {

    protected String messageId;

    public Rpc(final Rpc rpc) {
        super(rpc);
        this.messageId = rpc.messageId;
    }

    protected Rpc(final Request request) {
        super(request);
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        if (level == 0 && "rpc".equalsIgnoreCase(tag)) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i += 1) {
                if (attributes.getQName(i).contains("message-id")) {
                    messageId = attributes.getValue(i);
                    break;
                }
            }
        } else if (level == 1) {
            return createOperation(tag);
        }
        return this;
    }

    private Operation createOperation(final String tag) {
        switch (tag) {
            case "close-session":
                return new CloseSession(this);
            case "kill-session":
                return new KillSession(this);
            case "get":
                return new Get(this);
            case "get-config":
                return new GetConfig(this);
            case "edit-config":
                return new EditConfig(this);
            case "create-subscription":
                return new CreateSubscription(this);
            case "lock":
                return new Lock(this);
            case "unlock":
                return new Unlock(this);
            case "validate":
                return new Validate(this);
            case "commit":
                return new Commit(this);
            case "discard-changes":
                return new DiscardChanges(this);
            case "action":
                return new Action(this);
            case "copy-config":
                return new CopyConfig(this);
            case "get-schema":
                return new GetSchema(this);
            default:
                return new CustomOperation(this);
        }
    }

    @Override
    public Operation sendError(final CommandListener commandListener, final String errorMessage, final PrintWriter out) {
        final String errorMessageXml = errorMessage.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;");
        final String rpcError = String.format(RpcReplyFormat.RPC_ERROR_FAILED_TO_PARSE_XML, messageId, errorMessageXml);
        commandListener.sendError(messageId, rpcError, out);
        return new Nop();
    }
}
