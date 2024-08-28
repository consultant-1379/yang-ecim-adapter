/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.SshConfiguration;
import org.apache.sshd.server.session.ServerSession;

import java.util.Map;

public class NetconfPasswordAuthenticator implements PasswordAuthenticator {

    private final SshConfiguration configuration;

    public NetconfPasswordAuthenticator(final SshConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean authenticate(final String u, final String p, final ServerSession session) {
        final Map<String, String> users = configuration.getUsers();
        if (users != null) {
            for (final Map.Entry<String, String> user : users.entrySet()) {
                if (user.getKey().equals(u) && user.getValue().equals(p)) {
                    return true;
                }
            }
        }
        return !configuration.isAllowUsersFromListOnly();
    }
}
