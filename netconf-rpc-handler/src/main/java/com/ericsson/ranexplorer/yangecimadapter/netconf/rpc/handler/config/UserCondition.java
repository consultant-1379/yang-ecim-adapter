/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import org.apache.sshd.server.Environment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user-condition")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAddToContext
public class UserCondition extends Condition {

    @XmlAttribute(required = true)
    private String username;

    public UserCondition() {
    }

    public UserCondition(final String username) {
        this.username = username;
    }

    @Override
    public boolean apply(final Object obj) {
        if (obj instanceof Environment) {
            final String user = ((Environment) obj).getEnv().get(Environment.ENV_USER);
            return username.equals(user);
        }
        return false;
    }
}
