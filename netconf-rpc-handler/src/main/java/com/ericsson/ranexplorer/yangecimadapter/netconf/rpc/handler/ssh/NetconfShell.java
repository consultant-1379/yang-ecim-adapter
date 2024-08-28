/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.Configuration;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

/**
 * 
 * @author ebialan
 */
public class NetconfShell implements Command, SessionAware {

    private final Configuration configuration;
    private ExitCallback callback;
    private InputStream in;
    private OutputStream out;
    private SshNetconfSession session;
    private ServerSession serverSession;

    protected NetconfShell(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setInputStream(final InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(final OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(final OutputStream err) {
        //not implemented
    }

    @Override
    public void setExitCallback(final ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(final Environment env) throws IOException {
        final CommandListener commandListener = this.configuration.getListener(env);
        String username = env.getEnv().get(Environment.ENV_USER);
        this.session = new SshNetconfShellSession(callback, in, out, commandListener, configuration, username, serverSession);
        new Thread(this.session).start();
    }

    @Override
    public void destroy() {
        this.session.destroy();
    }

    @Override
    public void setSession(ServerSession session) {
        this.serverSession = session;
    }

}
