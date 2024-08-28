/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.api;

public enum RFC4741 {
    BASE("urn:ietf:params:netconf:base:1.0"), WRITABLE_RUNNING(
            "urn:ietf:params:netconf:capability:writable-running:1.0"), CANDIDATE(
            "urn:ietf:params:netconf:capability:candidate:1.0"), ROLLBACK_ON_ERROR(
            "urn:ietf:params:netconf:capability:rollback-on-error:1.0"), VALIDATE(
            "urn:ietf:params:netconf:capability:validate:1.0"), STARTUP(
            "urn:ietf:params:netconf:capability:startup:1.0");

    public final String urn;

    private RFC4741(String urn) {
        this.urn = urn;
    }
}
