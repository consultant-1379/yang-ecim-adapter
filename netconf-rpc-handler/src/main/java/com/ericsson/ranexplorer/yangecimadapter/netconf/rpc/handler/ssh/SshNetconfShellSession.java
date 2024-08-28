/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.Configuration;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.session.ServerSession;

/**
 * 
 * @author ebialan
 */
public class SshNetconfShellSession extends SshNetconfSession {

    /**
     * @param callback
     * @param in
     * @param out
     * @param commandListener
     * @param configuration
     */
    public SshNetconfShellSession(final ExitCallback callback, final InputStream in, final OutputStream out,
                                  final CommandListener commandListener, final Configuration configuration,
                                  final String user, ServerSession serverSession) {
        super(callback, in, out, commandListener, configuration, user, serverSession);
    }

    @Override
    public void run() {

        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println("prompt#");
        super.run();
    }

}
