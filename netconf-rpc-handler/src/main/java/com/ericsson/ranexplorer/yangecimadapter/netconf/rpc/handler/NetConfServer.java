/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler;

import java.io.File;
import java.util.*;

import javax.xml.bind.*;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.*;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.tls.TlsNetconfServer;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh.SshNetconfServer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetConfServer {

    private static final Logger logger = LoggerFactory.getLogger(NetConfServer.class);

    protected final SimpleServer server;

    public NetConfServer(final Configuration configuration) {
        this(configuration, System.getProperty("user.dir"));
    }

    public NetConfServer(final Configuration configuration, final String workingDir) {
        if (configuration instanceof SshConfiguration) {
            this.server = new SshNetconfServer((SshConfiguration) configuration, workingDir);
        } else {
            this.server = new TlsNetconfServer((TlsConfiguration) configuration);
        }
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop();
    }

    public static Configuration getConfiguration(final File configurationFile) {
        try {
            final Reflections reflections = new Reflections("com.ericsson.oss.mediation.netconf.config");
            final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(XmlAddToContext.class);
            classes.add(Configuration.class);
            classes.add(PrincipalCondition.class);
            classes.add(UserCondition.class);
            classes.add(Clazz.class);
            final JAXBContext jc = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
            final Unmarshaller u = jc.createUnmarshaller();
            return (Configuration) u.unmarshal(configurationFile);
        } catch (final JAXBException e) {
            logger.error("I wasn't able to parse configuration file {}.",
                    configurationFile.getPath(), e);
            throw new IllegalStateException("Failed to parse configuration!", e);
        }
    }

    public int getPort() {
        return this.server.getPort();
    }
}
