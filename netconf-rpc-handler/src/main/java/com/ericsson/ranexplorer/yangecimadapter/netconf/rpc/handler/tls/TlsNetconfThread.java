/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.tls;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.TlsConfiguration;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.DefaultCommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh.Killable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;

public class TlsNetconfThread extends Thread {

    private final Logger log = LoggerFactory.getLogger(TlsNetconfThread.class);
    private final ServerSocket serverSocket;
    private final Semaphore connectionPermits;
    private final Map<Integer, TlsNetconfSession> sessionThreads;
    private volatile boolean shuttingDown;
    private final ExecutorService executorService;
    private final TlsConfiguration tlsConfiguration;

    public TlsNetconfThread(TlsConfiguration configuration, ServerSocket socket) {
        super(TlsNetconfThread.class.getName() + "." + configuration.getPort());
        this.serverSocket = socket;
        this.tlsConfiguration = configuration;
        int countOfConnectionPermits = tlsConfiguration.getMaxConnections();
        this.connectionPermits = new Semaphore(countOfConnectionPermits);
        this.sessionThreads = new Hashtable<>(countOfConnectionPermits * 4 / 3 + 1);
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        this.runAcceptLoop(new NetconfSSLContext().getSSLSocketFactory(this.tlsConfiguration));
    }

    private void runAcceptLoop(final SSLSocketFactory sf) {
        while (!this.shuttingDown) {
            try {
                connectionPermits.acquire();
            } catch (InterruptedException consumed) {//NOSONAR
                continue;
            }

            final Socket socket;
            final SSLSocket sslSocket;
            try {
                socket = this.serverSocket.accept();
                sslSocket = startTls(socket, sf);
            } catch (IOException e) {
                connectionPermits.release();
                if (!this.shuttingDown) {
                    log.error("Error accepting connection", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException consumed) {
                        Thread.currentThread().interrupt();
                    }
                }
                continue;
            }

            final TlsNetconfSession session;
            try {
                final InputStream is = sslSocket.getInputStream();
                final OutputStream os = sslSocket.getOutputStream();
                session = new TlsNetconfSession(new TlsServerCallback() {

                    @Override
                    public void hello(final int sessionId, final TlsNetconfSession session) {
                        synchronized (sessionThreads) {
                            sessionThreads.put(sessionId, session);
                        }
                    }

                    @Override
                    public void killSession(int sessionId) {
                        synchronized (sessionThreads) {
                            Killable killable = sessionThreads.get(sessionId);
                            if (killable != null) {
                                killable.kill();
                            }
                        }
                    }

                    @Override
                    public void onExit(int sessionId) {
                        try {
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Future<?> future = executor.submit(this::awaitForEndOfStream);
                            if (tlsConfiguration.getWaitForClose() > 0) {
                                try {
                                    future.get(tlsConfiguration.getWaitForClose(), TimeUnit.MILLISECONDS);
                                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                                    log.error("I tried to read input stream entirely within {} ms but failed",
                                            tlsConfiguration.getWaitForClose(), e);
                                }
                            } else {
                                try {
                                    future.get();
                                } catch (InterruptedException | ExecutionException e) {
                                    log.warn("I tried to read input stream entirely ms but failed", e);
                                }
                            }
                            sslSocket.close();
                        } catch (IOException e) {
                            log.error("Error occurred when closing netconf socket with session id {}", sessionId, e);
                        }
                        sessionEnded(sessionId);
                    }

                    private void awaitForEndOfStream() {
                        try {
                            while (is.read() != -1){
                                //waiting for end of stream
                            }
                        } catch (IOException e) {
                            log.error("I tried to read input stream entirely but failed", e);
                        }
                    }
                }, is, os, findSuitableListener(sslSocket), tlsConfiguration);
            } catch (IOException e) {
                connectionPermits.release();
                log.error("Error while starting a connection", e);
                try {
                    socket.close();
                } catch (IOException e1) {
                    log.debug("Cannot close socket after exception", e1);
                }
                continue;
            }

            try {
                executorService.execute(session);
            } catch (RejectedExecutionException e) {
                connectionPermits.release();
                log.error("Error while executing a session", e);
                try {
                    socket.close();
                } catch (IOException e1) {
                    log.debug("Cannot close socket after exception", e1);
                }
            }
        }
    }

    private CommandListener findSuitableListener(SSLSocket sslSocket) {
        try {
            Principal principal = sslSocket.getSession().getPeerPrincipal();
            log.info("Principal: {}", principal);
            CommandListener listener = this.tlsConfiguration.getListener(principal);
            return listener != null ? listener : new DefaultCommandListener();
        } catch (SSLPeerUnverifiedException e) {
            log.warn("I wasn't able to authenticate client. Default listener will be used", e);
        }
        return new DefaultCommandListener();
    }

    private SSLSocket startTls(Socket socket, SSLSocketFactory sf) throws IOException {
        SSLSocket s = createSSLSocket(socket, sf);
        s.startHandshake();
        return s;
    }

    private SSLSocket createSSLSocket(Socket socket, SSLSocketFactory sf) throws IOException {
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        //socket is used outside this method, so no need to close it here
        SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));//NOSONAR
        s.setUseClientMode(false);
        s.setWantClientAuth(true);
        s.setSoTimeout((int)this.tlsConfiguration.getSocketTimeout());
        s.setEnabledProtocols(tlsConfiguration.getSupportedProtocols().toArray(
                new String[tlsConfiguration.getSupportedProtocols().size()]));
        s.setEnabledCipherSuites(tlsConfiguration.getSupportedCiphers().toArray(
                new String[tlsConfiguration.getSupportedCiphers().size()]));
        return s;
    }

    public void shutdown() {
        shutdownServerThread();
        shutdownSessions();
    }

    private void shutdownServerThread() {
        shuttingDown = true;
        closeServerSocket();
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeServerSocket() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            log.error("Failed to close server socket.", e);
        }
    }

    private void shutdownSessions() {
        Map<Integer, TlsNetconfSession> sessionsToBeClosed;
        synchronized (this) {
            sessionsToBeClosed = new HashMap<>(sessionThreads);
        }
        for (TlsNetconfSession sessionThread : sessionsToBeClosed.values()) {
            sessionThread.kill();
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(
                            this.tlsConfiguration.getWaitForClose() > 0 ?
                                    this.tlsConfiguration.getWaitForClose() : Integer.MAX_VALUE,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted waiting for termination of session threads", e);
            Thread.currentThread().interrupt();
        }
    }

    public void sessionEnded(int sessionId) {
        synchronized (this) {
            sessionThreads.remove(sessionId);
        }
        connectionPermits.release();
    }

}
