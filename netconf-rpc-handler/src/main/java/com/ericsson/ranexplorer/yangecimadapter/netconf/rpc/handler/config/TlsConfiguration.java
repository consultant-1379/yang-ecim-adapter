/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement(name = "TlsConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class TlsConfiguration extends Configuration {

    private static final List<String> DEFAULT_CIPHERS = Collections.singletonList("TLS_RSA_WITH_AES_128_CBC_SHA");
    private static final List<String> DEFAULT_PROTOCOLS = Collections.singletonList("TLSv1");

    @XmlAttribute
    private int maxConnections;

    @XmlElementWrapper
    @XmlElement(name = "supportedCipher")
    private List<String> supportedCiphers;

    @XmlElementWrapper
    @XmlElement(name = "supportedProtocol")
    private List<String> supportedProtocols;

    @XmlElementWrapper(required = true)
    @XmlElement(name = "securityDefinition")
    private List<SecurityDefinition> securityDefinitions;

    @XmlElementWrapper
    @XmlElement(name = "trustedCertificate")
    private List<TrustedCertificate> trustedCertificates;

    public TlsConfiguration() {
        //default constructor for JAXB parser
    }

    public TlsConfiguration(int port, List<NetconfRule> rules, List<SecurityDefinition> securityDefinitions) {
        this(0, 0L, port, 50, rules,
                new ArrayList<>(DEFAULT_CIPHERS), new ArrayList<>(DEFAULT_PROTOCOLS),
                securityDefinitions, Collections.emptyList());
    }

    public TlsConfiguration(int socketTimeout, long waitForClose, int port, int maxConnections, List<NetconfRule> rules,
                            List<String> supportedCiphers, List<String> supportedProtocols,
                            List<SecurityDefinition> serverCertificates, List<TrustedCertificate> trustedCertificates) {
        super(socketTimeout, waitForClose, port, rules);
        this.maxConnections = maxConnections;
        this.supportedCiphers = supportedCiphers;
        this.supportedProtocols = supportedProtocols;
        this.securityDefinitions = serverCertificates;
        this.trustedCertificates = trustedCertificates;
    }

    public List<String> getSupportedCiphers() {
        return supportedCiphers;
    }

    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    public List<SecurityDefinition> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public List<TrustedCertificate> getTrustedCertificates() {
        return trustedCertificates;
    }

    public int getMaxConnections() {
        return maxConnections;
    }
}
