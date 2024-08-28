/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class TrustedCertificate {

    @XmlAttribute(required = true)
    private String alias;

    @XmlValue
    private String certificate;

    public TrustedCertificate() {}

    public TrustedCertificate(String alias, String certificate) {
        this.alias = alias;
        this.certificate = certificate;
    }

    public String getAlias() {
        return alias;
    }

    public String getCertificate() {
        return certificate;
    }
}
