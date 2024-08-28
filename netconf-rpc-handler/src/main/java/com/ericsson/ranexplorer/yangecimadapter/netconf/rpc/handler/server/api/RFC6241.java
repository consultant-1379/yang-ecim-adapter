/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.api;

public enum RFC6241 {
    BASE("urn:ietf:params:netconf:base:1.1"), VALIDATE("urn:ietf:params:netconf:capability:validate:1.1");

    public final String urn;

    private RFC6241(String urn) {
        this.urn = urn;
    }
}
