
/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetConfServer;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.*;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Subparsers;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterServer {

    private static final Logger logger = LoggerFactory.getLogger(AdapterServer.class);

    public static void main(final String[] args) {
        logger.info("Starting...");
        final ArgumentParser parser = getArgumentParser();
        final NetConfServer sshd;
        try {
            final Namespace namespace = parser.parseArgs(args);
            final String action = namespace.getString("action");
            final File workingDirParam = namespace.get("workdir");
            final Configuration configuration;
            switch (action) {
                case "ssh":
                    final String nodeAddress = namespace.getString("node");
                    final int nodePort = namespace.getInt("nodeport");
                    final String userParam = namespace.getString("username");
                    final String passParam = namespace.getString("password");
                    final int portParam = namespace.getInt("port");
                    final String adapterBindAddress = namespace.getString("bind_adapter");
                    final String nodeBindAddress = namespace.getString("bind_node");
                    final Map<String, String> users = new HashMap<>();
                    users.put(userParam, passParam);
                    String classStr = namespace.getString("class");
                    final long idleTimeout = Long.parseLong(namespace.getString("idle_timeout"));
                    final long disconnectTimeout = Long.parseLong(namespace.getString("disconnect_timeout"));
                    if (classStr == null) {
                        throw new IllegalStateException("configuration is missing, cannot start server!");
                    }
                    configuration = new SshConfiguration(idleTimeout, disconnectTimeout, portParam, Collections.singletonList(new NetconfRule(new Then(
                            new Clazz(classStr)))), true, users);
                    configuration.setNodeAddress(nodeAddress);
                    configuration.setNodePort(nodePort);
                    configuration.setAdapterBindAddress(adapterBindAddress);
                    configuration.setNodeBindAddress(nodeBindAddress);

                    break;
                case "configuration":
                    final File configurationFile = namespace.get("config");
                    configuration = NetConfServer.getConfiguration(configurationFile);
                    break;
                default:
                    throw new IllegalStateException("configuration is missing, cannot start server!");
            }

            sshd = new NetConfServer(configuration,
                    workingDirParam != null ? workingDirParam.getAbsolutePath() : System.getProperty("user.dir"));
            sshd.start();
            Runtime.getRuntime().addShutdownHook(new Thread(sshd::stop));
            logger.info("Started...");

            while (true) {//NOSONAR
                Thread.sleep(10L * 10000);
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (InterruptedException e) {
            logger.warn("Application was interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            logger.info("Finished...");
        }
    }

    private static ArgumentParser getArgumentParser() {
        final ArgumentParser parser = ArgumentParsers.newFor("netconf-server").build().defaultHelp(true);
        final Argument argument = parser.addArgument("-w", "--workdir").type(Arguments.fileType().verifyExists());
        try {
            argument.setDefault(new File(new File(AdapterServer.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI()).getParent()));
        } catch (URISyntaxException e) {
            logger.error("I wasn't able to get a current directory of netconf server", e);
        }
        final Subparsers subparsers = parser.addSubparsers().dest("action");
        final ArgumentParser simpleServerParser = subparsers.addParser("ssh");
        simpleServerParser.addArgument("--port").type(Integer.class).choices(Arguments.range(1, 65535)).setDefault(22);
        simpleServerParser.addArgument("--nodeport").type(Integer.class).choices(Arguments.range(1, 65535)).setDefault(22);
        simpleServerParser.addArgument("-u", "--username").required(true);
        simpleServerParser.addArgument("-p", "--password").required(true);
        simpleServerParser.addArgument("-n", "--node");
        simpleServerParser.addArgument("-a", "--bind_adapter");
        simpleServerParser.addArgument("-b", "--bind_node");
        simpleServerParser.addArgument("-i", "--idle_timeout").setDefault(TimeUnit.MINUTES.toMillis(10));
        simpleServerParser.addArgument("-d", "--disconnect_timeout").setDefault(TimeUnit.SECONDS.toMillis(10));
        final MutuallyExclusiveGroup storyGroup = simpleServerParser.addMutuallyExclusiveGroup();
        storyGroup.addArgument("-c", "--class");
        final ArgumentParser configuredServerParser = subparsers.addParser("configuration");
        configuredServerParser.addArgument("-c", "--config").required(true)
                .type(Arguments.fileType().verifyExists().verifyCanRead());
        return parser;
    }

}
