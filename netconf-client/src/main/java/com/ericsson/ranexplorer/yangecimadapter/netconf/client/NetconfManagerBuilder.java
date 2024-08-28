/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.client;

import com.ericsson.oss.mediation.transport.api.TransportManager;
import com.ericsson.oss.mediation.transport.api.TransportManagerCI;
import com.ericsson.oss.mediation.transport.manager.TransportManagerFactory;
import com.ericsson.oss.mediation.util.netconf.api.NetconManagerConstants;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.manager.NetconfManagerFactory;
import com.ericsson.ranexplorer.yangecimadapter.common.services.netconf.capabilities.CapabilityService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Netconf manager builder
 * 
 * @author enendju
 *
 */

public class NetconfManagerBuilder {
    private int sessionId;
    final TransportManagerCI transportManagerCI;
    final Map<String, Object> properties;
    String transportType;

    public NetconfManagerBuilder(final String connType, final int sessionId) {
        properties = new HashMap<>();
        transportType = connType;
        transportManagerCI = new TransportManagerCI().setSocketTimeout(100000);
        this.sessionId = sessionId;
    }

    public NetconfManagerBuilder stdCapabilities() {
        properties.put(NetconManagerConstants.CAPABILITIES_KEY, new CapabilityService().getCapabilitiesAsList("capabilities.to.node"));
        return this;
    }

    public NetconfManagerBuilder capabilities(final List<String> capabilities) {
        properties.put(NetconManagerConstants.CAPABILITIES_KEY, capabilities);
        return this;
    }

    public NetconfManagerBuilder ssh() {
        this.transportType = "SSH";
        return this;
    }

    public NetconfManagerBuilder tls() {
        this.transportType = "TLS";
        return this;
    }

    public NetconfManagerBuilder hostName(final String host) {
        transportManagerCI.setHostname(host);
        return this;
    }
    
    public NetconfManagerBuilder port(final int port) {
        transportManagerCI.setPort(port);
        return this;
    }

    public NetconfManagerBuilder socketConnectionTimeoutInMillis(final int socketConnectionTimeoutInMillis) {
        transportManagerCI.setSocketConnectionTimeoutInMillis(socketConnectionTimeoutInMillis);
        return this;
    }

    public NetconfManagerBuilder idleConnectionTimeoutSeconds(final int idleConnectionTimeoutSeconds) {
        transportManagerCI.setIdleConnectionTimeoutSeconds(idleConnectionTimeoutSeconds);
        return this;
    }

    public NetconfManagerBuilder localAddress(final String localAddress) {
        transportManagerCI.setLocalAddress(localAddress);
        return this;
    }

    public NetconfManagerBuilder credentials(final String username, final String password) {
        transportManagerCI.setUsername(username);
        transportManagerCI.setPassword(password);
        return this;
    }

    public NetconfManagerBuilder serverCertificate(final URL url) throws NetconfManagerException {
        transportManagerCI.setServerCertToTrust(loadChars(url));
        return this;
    }

    private char[] loadChars(final URL url) throws NetconfManagerException {
        try {
            return new String(Files.readAllBytes(Paths.get(url.toURI())), StandardCharsets.UTF_8).toCharArray();
        } catch (IOException | URISyntaxException e) {
            throw new NetconfManagerException("I wasn't able to load chars from this url " + url, e);
        }
    }

    public NetconfManager build() throws NetconfManagerException {
        //using wrapper around original TransportManager for asynch reading
        final TransportManager transportManager = TransportManagerFactory.createTransportManager(
                this.transportType, transportManagerCI);
        return NetconfManagerFactory.createNetconfManager(new AdapterTransportManager(transportManager, sessionId), properties);
    }

    public NetconfManagerBuilder clientKeyAndCertificates(final URL keyUrl, final URL certificatesUrl)
            throws NetconfManagerException {
        transportManagerCI.setClientPrivateKey(loadChars(keyUrl));
        transportManagerCI.setClientCerts(loadChars(certificatesUrl));
        return this;
    }
}
