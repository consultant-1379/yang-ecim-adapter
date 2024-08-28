/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.SimpleServer;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.SshConfiguration;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class SshNetconfServer implements SimpleServer {

    protected final SshServer sshd;

    public  SshNetconfServer(final SshConfiguration configuration, final String workingDir) {
        this.sshd = SshServer.setUpDefaultServer();
        this.sshd.setPort(configuration.getPort());
        this.sshd.setHost(configuration.getAdapterBindAddress());
        Map<String, String> options = new LinkedHashMap<>();
        options.put(FactoryManager.SOCKET_KEEPALIVE, String.valueOf(true));
        //option to avoid UnixAsynchronousSocket timeout
        options.put(FactoryManager.NIO2_READ_TIMEOUT, String.valueOf(0));
        if(configuration.getSocketTimeout() != 0) {
            options.put(FactoryManager.IDLE_TIMEOUT, String.valueOf(configuration.getSocketTimeout()));
        }
        if(configuration.getWaitForClose() != 0) {
            options.put(FactoryManager.DISCONNECT_TIMEOUT, String.valueOf(configuration.getWaitForClose()));
        }
        if(!options.isEmpty()) {
            sshd.getProperties().putAll(options);
        }
        sshd.setShellFactory(() ->
            // Auto-generated method stub
            new NetconfShell(configuration)
        );
        this.sshd.setSubsystemFactories(NetconfSubsystemFactoryHelper.getNetconfSubsystemFactories(configuration));
        this.sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(workingDir, "hostkey.ser")));
        this.sshd.setPasswordAuthenticator(new NetconfPasswordAuthenticator(configuration));
    }

    @Override
    public void start() {
        try {
            this.sshd.start();
        } catch (final IOException e) {
            throw new NetconfServerException(String.format("I wasn't able to start netconf server over ssh protocol on port %d", this.sshd.getPort()), e);
        }
    }

    @Override
    public void stop() {
        try {
            this.sshd.stop();
        } catch (IOException e) {
            throw new NetconfServerException("Error occurred when stopping netconf server", e);
        }
    }

    @Override
    public int getPort() {
        return this.sshd.getPort();
    }
}
