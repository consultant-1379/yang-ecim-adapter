/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.api;

public enum RFC5277 {
    NOTIFICATION("urn:ietf:params:netconf:capability:notification:1.0");

    public final String urn;

    private RFC5277(String urn) {
        this.urn = urn;
    }
}
