/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.api;

public enum Operation {
    NOT_REQUESTED("not-requested"), MERGE("merge"), CREATE("create"), DELETE("delete"), REPLACE("replace"), REMOVE(
            "remove");

    private String parameterName;

    Operation(final String parameterName) {
        this.parameterName = parameterName;
    }

    public String asParameter() {
        return this.parameterName;
    }
}
