/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

public enum FilterBlockName {

    ENODEB_FUNCTION("enodeb-function"), NODE_SUPPORT("node-support"), EQUIPMENT("equipment"), NETCONF_STATE("netconf-state"), EVENT_STREAM(
            "netconf"), UNKNOWN("unknown");

    private String tagName;

    private static final FilterBlockName[] VALUES = values();

    private FilterBlockName(final String tagName) {
        this.tagName = tagName;
    }

    private String getTagName() {
        return tagName;
    }

    public static FilterBlockName fromTagName(final String tagName) {
        for (final FilterBlockName filterBlockName : VALUES) {
            if (tagName.equals(filterBlockName.getTagName())) {
                return filterBlockName;
            }
        }
        return FilterBlockName.UNKNOWN;
    }

}
