/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;

@XmlAccessorType(XmlAccessType.FIELD)
public class Then {
    @XmlElementRef
    private Action action;

    public Then() {
    }

    public Then(final Action action) {
        this.action = action;
    }

    public CommandListener apply(final Object sttmt) {
        return action.apply(sttmt);
    }
}
