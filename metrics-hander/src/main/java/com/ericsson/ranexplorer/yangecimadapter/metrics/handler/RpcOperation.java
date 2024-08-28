/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

public enum RpcOperation {

    EDIT_CONFIG("edit-config"), GET("get"), GET_CONFIG("get-config"), CREATE_SUBSCRIPTION("create-subscription"), CLOSE_SESSION("close-session"), KILL_SESSION("kill-session"), LOCK(
            "lock"), UNLOCK("unlock"), GET_SCHEMA("get-schema"), UNKNOWN("unknown");

    private String tagName;

    private RpcOperation(final String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

}
