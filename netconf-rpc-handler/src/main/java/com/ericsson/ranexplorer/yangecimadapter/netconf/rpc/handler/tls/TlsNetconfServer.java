/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.tls;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.SimpleServer;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.TlsConfiguration;

import java.io.IOException;
import java.net.*;

public class TlsNetconfServer implements SimpleServer {

    protected final TlsConfiguration configuration;
    private static final int BACKLOG = 50;
    private TlsNetconfThread serverThread;

    public TlsNetconfServer(final TlsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() {
        ServerSocket serverSocket;
        try {
            serverSocket = this.createServerSocket();
        } catch (IOException e) {
            throw new NetconfServerException("I wasn't able to create tls server socket on port "
                    + this.configuration.getPort(), e);
        }
        this.serverThread = new TlsNetconfThread(configuration, serverSocket);
        this.serverThread.start();
    }

    @Override
    public void stop() {
        this.serverThread.shutdown();
        this.serverThread = null;
    }

    @Override
    public int getPort() {
        return configuration.getPort();
    }

    protected ServerSocket createServerSocket() throws IOException {
        return new ServerSocket(this.configuration.getPort(), BACKLOG);
    }

}
