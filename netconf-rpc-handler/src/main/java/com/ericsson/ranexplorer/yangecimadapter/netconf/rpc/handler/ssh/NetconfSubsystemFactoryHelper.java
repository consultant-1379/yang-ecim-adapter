/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.SshConfiguration;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class NetconfSubsystemFactoryHelper {

    private NetconfSubsystemFactoryHelper(){
        //just to satisfy sonar...
    }
    private static final Logger logger = LoggerFactory.getLogger(NetconfSubsystemFactoryHelper.class);

    public static List<NamedFactory<Command>> getNetconfSubsystemFactories(final SshConfiguration configuration) {
        List<NamedFactory<Command>> netconfSubsystemFactories = new ArrayList<>();
        for (String subsystem: configuration.getSubsystems()) {
           logger.info("Creating NetconfSubsystemFactory for \"{}\" subsystem", subsystem);  
           netconfSubsystemFactories.add(new NetconfSubsystemFactory(subsystem, configuration));
        }
        return netconfSubsystemFactories;
    }
 }
