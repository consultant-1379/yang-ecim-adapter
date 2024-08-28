/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.tls;

public interface TlsServerCallback {
    void killSession(int sessionId);

    void onExit(int sessionId);

    void hello(int sessionId, final TlsNetconfSession session);
}
