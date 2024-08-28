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
import javax.xml.bind.annotation.XmlRootElement;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlRootElement(name = "principal-condition")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAddToContext
public class PrincipalCondition extends Condition {

    protected final Pattern CN_PATTERN = Pattern.compile(".*cn=([^,]+).*", Pattern.CASE_INSENSITIVE);

    @XmlAttribute(required = true)
    private String cn;

    public PrincipalCondition() {
    }

    public PrincipalCondition(final String cn) {
        this.cn = cn;
    }

    @Override
    public boolean apply(final Object obj) {
        if (obj instanceof Principal) {
            Matcher m = CN_PATTERN.matcher(((Principal) obj).getName());
            return m.matches() && m.group(1).equals(cn);
        }
        return false;
    }
}
