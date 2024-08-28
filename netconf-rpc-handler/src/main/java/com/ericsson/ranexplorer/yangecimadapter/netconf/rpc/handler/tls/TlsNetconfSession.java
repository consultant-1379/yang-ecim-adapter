/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.tls;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.client.NetconfManagerBuilder;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.*;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.NetconfSession;

public class TlsNetconfSession extends NetconfSession {
    private static final Logger logger = LoggerFactory.getLogger(TlsNetconfSession.class);

    protected final TlsServerCallback callback;

    public TlsNetconfSession(final TlsServerCallback callback, final InputStream in, final OutputStream out,
                             final CommandListener commandListener, final Configuration configuration) {
        super(in, out, commandListener, configuration);
        this.callback = callback;
    }

    @Override
    public void hello(final int sessionId, final PrintWriter out) {
        super.hello(sessionId, out);
        callback.hello(sessionId, this);
    }

    @Override
    public void killSession(final String messageId, final int sessionId, final String southSessionId, final PrintWriter out) {
        super.killSession(messageId, sessionId, southSessionId, out);
        if (sessionId != this.sessionId) {
            callback.killSession(sessionId);
        }
    }

    @Override
    protected void onExit(final int exitValue, final String exitMessage) {
        callback.onExit(this.sessionId);
    }

    @Override
    protected void onExit(final int exitValue) {
        callback.onExit(this.sessionId);
    }

    @Override
    public NetconfManager getNetconfManager() {
        return commandListener.getNetconfManager();
    }

    @Override
    public void setNetconfManager(NetconfManager netconfManager) {
        commandListener.setNetconfManager(netconfManager);
    }

    @Override
    public void setSubscribedState(boolean subscribed) {
        commandListener.setSubscribedState(subscribed);
    }

    @Override
    public void setSessionId(int sessionId) {
        commandListener.setSessionId(sessionId);
    }

    @Override
    protected NetconfManager buildManager(String nodeAddress, int nodePort) throws NetconfManagerException {
        NetconfManagerBuilder nmr = new NetconfManagerBuilder("TLS", sessionId);
        TlsConfiguration tlsConfiguration = (TlsConfiguration) configuration;
        nmr.hostName(nodeAddress).port(nodePort).idleConnectionTimeoutSeconds((int) configuration.getSocketTimeout())
                .localAddress(configuration.getNodeBindAddress());
        //todo check TLS actual behaviour here //NOSONAR
        try {
            List<TrustedCertificate> trustedCertificateList = tlsConfiguration.getTrustedCertificates();
            for (TrustedCertificate c : trustedCertificateList) {

                nmr.serverCertificate(new URL(c.getCertificate()));

            }
            List<SecurityDefinition> securityDefinitions = tlsConfiguration.getSecurityDefinitions();
            for (SecurityDefinition s : securityDefinitions) {
                nmr.clientKeyAndCertificates(new URL(s.getKey()), new URL(s.getCertificate()));
            }
        } catch (MalformedURLException e) {
            logger.error("Error Occurred: {}", e.getMessage());
        }
        return nmr.stdCapabilities().build();
    }

    @Override
    protected int getDefaultPort() {
        return 636;
    }

    @Override
    protected void shutdown() {
        // Needs to be implemented if/when TLS is fully supported
    }
}
