/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;

import java.io.PrintWriter;

public class CloseSession extends Rpc {

    public CloseSession(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Nop invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.closeSession(messageId, out);
        return new Nop();
    }
}
