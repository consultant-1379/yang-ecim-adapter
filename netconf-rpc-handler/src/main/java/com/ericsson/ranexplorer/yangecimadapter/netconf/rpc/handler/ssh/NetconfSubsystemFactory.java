/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.Configuration;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;

public class NetconfSubsystemFactory implements NamedFactory<Command> {

    private final String name;
    private final Configuration configuration;

    public NetconfSubsystemFactory(final Configuration configuration) {
        this("netconf", configuration);
    }

    public NetconfSubsystemFactory(final String name, final Configuration configuration) {
        this.configuration = configuration;
        this.name = name; 
    }

    public Command create() {
        return new NetconfSubsystem(this.configuration);
    }

    public String getName() {
        return name;
    }
}
