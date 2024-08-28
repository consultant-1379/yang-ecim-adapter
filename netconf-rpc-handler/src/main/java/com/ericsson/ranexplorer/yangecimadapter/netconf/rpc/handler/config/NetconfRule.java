/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class NetconfRule {
    @XmlElement
    private When when;
    @XmlElement
    private Then then;

    public NetconfRule() {
    }

    public NetconfRule(final When when, final Then then) {
        this.when = when;
        this.then = then;
    }

    public NetconfRule(final Then then) {
        this(null, then);
    }

    public boolean when(final Object cond) {
        return when == null || when.apply(cond);
    }

    public CommandListener then(final Object sttmt) {
        return then.apply(sttmt);
    }
}
