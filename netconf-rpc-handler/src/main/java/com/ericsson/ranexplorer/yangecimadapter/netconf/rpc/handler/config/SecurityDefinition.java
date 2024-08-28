/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityDefinition {

    @XmlAttribute(required = true)
    private String alias;
    @XmlElement(required = true)
    private String key;
    @XmlElement(required = true)
    private String certificate;

    public SecurityDefinition() {
    }

    public SecurityDefinition(String alias, String key, String certificate) {
        this.alias = alias;
        this.key = key;
        this.certificate = certificate;
    }

    public String getAlias() {
        return alias;
    }

    public String getKey() {
        return key;
    }

    public String getCertificate() {
        return certificate;
    }
}
