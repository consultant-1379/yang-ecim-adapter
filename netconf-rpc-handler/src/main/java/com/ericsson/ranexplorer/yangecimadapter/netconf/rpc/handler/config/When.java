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
public class When {

    @XmlElementRef
    private Condition condition;

    public When() {
    }

    public When(final Condition condition) {
        this.condition = condition;
    }

    public boolean apply(final Object cond) {
        return condition.apply(cond);
    }
}
