/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerConnectionException;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MetricsHandler;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.AdapterListenerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.KillSessionException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.mediation.util.netconf.api.NetconfConnectionStatus;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.Configuration;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.NetconfServerParser;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh.Killable;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.oss.mediation.util.netconf.api.NetconfResponse;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.DefaultOperation;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.ErrorOption;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.TestOption;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;

public abstract class NetconfSession implements Runnable, CommandListener, Killable {

    private static final Logger logger = LoggerFactory.getLogger(NetconfSession.class);

    private static final AtomicInteger sessionIdGenerator = new AtomicInteger(1);
    private static final Map<Integer, String> killableSessionToRemoteMap = new ConcurrentHashMap<>();
    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;

    protected final InputStream in;
    protected final OutputStream out;
    protected final AtomicBoolean closed;
    protected int sessionId;
    protected String remoteSessionId;
    protected final CommandListener commandListener;
    protected final ExecutorService notificationSenderExecutor = Executors.newSingleThreadExecutor();
    protected final ExecutorService parserExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler((t, e) -> {
                kill();
                callOnExit(7, e.getMessage());
            })
            .build());
    protected final List<Future<Boolean>> notificationSenders = new ArrayList<>();
    protected final Configuration configuration;
    private NetconfSessionUtility netconfSessionUtility;

    public NetconfSession(final InputStream in, final OutputStream out, final CommandListener commandListener,
                          final Configuration configuration) {
        logger.info("NetconfSession created with listener {} ", commandListener);
        this.sessionId = sessionIdGenerator.getAndIncrement();
        METRICS_HANDLER.markStartCreateSession(sessionId);
        this.in = in;
        this.out = out;
        this.closed = new AtomicBoolean(false);
        this.commandListener = commandListener;
        this.configuration = configuration;
        this.netconfSessionUtility = new NetconfSessionUtility(commandListener, this);
    }

    @Override
    public void run() {
        int exitValue = 0;
        String exitMessage = null;
        try (final PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)){
            commandListener.setNetconfManager(initNetconfManager());
            commandListener.setSessionId(sessionId);
            logger.debug("This session id: {}", sessionId);
            NetconfResponse connectionResponse = connectNetconfManager();
            logger.info("Netconf Client connection established: {}", connectionResponse);
            this.hello(this.sessionId, pw);
            METRICS_HANDLER.markEndCreateSession(sessionId);
            final NetconfServerParser nsp = new NetconfServerParser(XMLReaderFactory.createXMLReader(), in, closed,sessionId, this, pw);
            parserExecutor.execute(nsp);
            while (!closed.get()) {
                Thread.sleep(1000);
            }
        } catch (final NetconfServerException e) {
            logger.error("Expected error occurred. I must close connection for the session with id {}", this.sessionId, e);
            exitValue = 4;
            exitMessage = "Error occurred: " + e.getMessage();
        } catch (final SAXException e) {
            logger.error("Failed to parse xml. I must close connection for the session with id {}", this.sessionId, e);
            exitValue = 2;
            exitMessage = "Wasn't able to create parser";
        } catch (NetconfManagerException e) {
            logger.info("Failed to connect to Node, check if running!", this.sessionId, e);
            exitValue = 3;
            exitMessage = "Node is not available";
        } catch (InterruptedException e) {
            logger.error("Thread sleep for session {} is interrupted!", this.sessionId, e);
            exitValue = 5;
            exitMessage = "Error occurred: " + e.getMessage();
            Thread.currentThread().interrupt();
        } catch (UnsupportedOperationException e){
            logger.info("Not supported operation executed in session with id {}!", this.sessionId, e);
            exitValue = 6;
            exitMessage = "Operation is not supported";
        }catch (NetconfServerConnectionException e){
            logger.info("Reconnection failed in session with id {}!", this.sessionId, e);
            exitValue = 7;
            exitMessage = "Reconnection failed";
        }
        finally {
            if (!parserExecutor.isTerminated()) {
                parserExecutor.shutdownNow();
                logger.info("SessionId {}> parser terminated!!!", sessionId);
            }
            this.stopNotificationSenders();
            METRICS_HANDLER.removeSession(sessionId);
            callOnExit(exitValue, exitMessage);
        }
    }

    private void callOnExit(int exitValue, String exitMessage ){
        if (exitMessage == null) {
            onExit(exitValue);
        } else {
            onExit(exitValue, exitMessage);
        }
    }

    protected NetconfManager initNetconfManager() throws NetconfManagerException {
        String nodeAddress = configuration.getNodeAddress();
        int nodePort = configuration.getNodePort();
        if (nodeAddress == null) {
            throw new NetconfServerException("Node IP address is not set!");
        }
        if (nodePort == 0) {
            nodePort = getDefaultPort();
        }
        return buildManager(nodeAddress, nodePort);
    }

    protected abstract NetconfManager buildManager(String nodeAddress, int nodePort) throws NetconfManagerException;

    protected abstract int getDefaultPort();

    public int getId() {
        return this.sessionId;
    }

    @Override
    public void closeSession(final String messageId, final PrintWriter out) {
        this.commandListener.closeSession(messageId, out);
        killableSessionToRemoteMap.remove(sessionId);
        commandListener.setSubscribedState(false);
        this.closed.set(true);
    }

    @Override
    public void hello(final int sessionId, final PrintWriter out) {
        this.commandListener.hello(sessionId, out);
    }

    @Override
    public void clientHello(final List<String> capabilities, final PrintWriter out) {
        this.commandListener.clientHello(capabilities, out);
    }

    @Override
    public void get(final String messageId, final Filter filter, final PrintWriter out) {
        try{
            netconfSessionUtility.reconnectNetconfManagerIfDisconnected();
            this.commandListener.get(messageId, filter, out);
        }catch (AdapterListenerException e){
            logger.error("AdapterListenerException occurred during get operation!", e);
            netconfSessionUtility.retryGetOperation(messageId, filter, out);
        }
    }

    @Override
    public void getConfig(final String messageId, final Datastore datastore, final Filter filter, final PrintWriter out) {
        try{
            netconfSessionUtility.reconnectNetconfManagerIfDisconnected();
            this.commandListener.getConfig(messageId, datastore, filter, out);
        }catch (AdapterListenerException e){
            logger.error("AdapterListenerException occurred during getConfig operation!", e);
            netconfSessionUtility.retryGetConfigOperation(messageId, datastore, filter, out);
        }

    }

    @Override
    public void lock(final String messageId, final String sessionid, final Datastore target, final PrintWriter out) {
        this.commandListener.lock(messageId, String.valueOf(this.sessionId), target, out);
    }

    @Override
    public void unlock(final String messageId, final Datastore target, final PrintWriter out) {
        this.commandListener.unlock(messageId, target, out);
    }

    @Override
    public void validate(final String messageId, final Datastore source, final PrintWriter out) {
        this.commandListener.validate(messageId, source, out);
    }

    @Override
    public void commit(final String messageId, final PrintWriter out) {
        this.commandListener.commit(messageId, out);
    }

    @Override
    public void discardChanges(final String messageId, final PrintWriter out) {
        this.commandListener.discardChanges(messageId, out);
    }

    @Override
    public void editConfig(final String messageId, final Datastore datastore, final DefaultOperation defaultOperation, final ErrorOption errorOption,
                           final TestOption testOption, final String config, final PrintWriter out) {
        try{
            netconfSessionUtility.reconnectNetconfManagerIfDisconnected();
            this.commandListener.editConfig(messageId, datastore, defaultOperation, errorOption, testOption, config, out);
        }catch (AdapterListenerException e){
            logger.error("AdapterListenerException occurred during editConfig operation!", e);
            netconfSessionUtility.retryEditConfigOperation(messageId, datastore, defaultOperation, errorOption, testOption, config, out);
        }

    }

    @Override
    public void killSession(final String messageId, final int sessionId, final String southSessionId, final PrintWriter out) {
        String session = killableSessionToRemoteMap.get(sessionId);
        try{
            this.commandListener.killSession(messageId, sessionId, session, out);
        }catch (KillSessionException e){
            logger.error("failed to kill session {}", session, e);
            throw new KillSessionException(e);
        }
    }

    @Override
    public Callable<Boolean> createSubscription(final String messageId, final String stream, final Filter filter, final String startTime,
                                                final String stopTime, final PrintWriter out) {
        Callable<Boolean> notificationSender;
        try{
            notificationSender = createNotificationSender(messageId, stream, filter, startTime, stopTime, out);
            commandListener.setSubscribedState(true);
        }catch (AdapterListenerException e){
            logger.error("AdapterListenerException occurred during createSubscription operation!", e);
            notificationSender = netconfSessionUtility.retryCreateSubscriptionOperation(messageId, stream, filter, startTime, stopTime, out);
        }

        return notificationSender;
    }

    Callable<Boolean> createNotificationSender(String messageId, String stream, Filter filter, String startTime, String stopTime, PrintWriter out) {

        Callable<Boolean> notificationSender = this.commandListener.createSubscription(messageId, stream, filter, startTime, stopTime, out);
        if (notificationSender != null) {
                notificationSenders.add(this.notificationSenderExecutor.submit(notificationSender));
        }
        return notificationSender;
    }



    @Override
    /**
     * Method can be called several times so no code that can be influenced by this behaviour should be added here
     */
    public void kill() {
        String session = killableSessionToRemoteMap.remove(sessionId);
        try {
            //probably not necessary, but to be on the safe side, additional check on complete session.
            if(getNetconfManager().getStatus() == NetconfConnectionStatus.CONNECTED) {
                getNetconfManager().disconnect();
            }
        } catch (NetconfManagerException e) {
            logger.warn("failed to disconnect session {}!", session, e);
        }
        commandListener.setSubscribedState(false);
        this.closed.set(true);
        parserExecutor.shutdownNow();
        shutdown();
    }

    @Override
    public void sendError(final String messageId, final String rpcError, final PrintWriter out) {
        this.commandListener.sendError(messageId, rpcError, out);
    }

    /**
     * This method should be called before closing the connection whatever is the reason.
     *
     * @author ebialan
     */
    private void stopNotificationSenders() {
        try {
            for (final Future<Boolean> notificationSender : notificationSenders) {
                if (notificationSender != null && !notificationSender.isDone()) {
                    logger.info("Terminating the notification sender ...");
                    notificationSender.cancel(true);
                }
            }
        } catch (final Exception ex) {
            logger.error("Exception occurred while stopping notifications", ex);
        } finally {
            if (!notificationSenderExecutor.isTerminated()) {
                notificationSenderExecutor.shutdownNow();
                logger.info("SessionId {}> NotificationSenderExecutor terminated!!!", sessionId);
            }
        }
    }

    @Override
    public void action(final String messageId, final String actionMessage, final PrintWriter out) {
        this.commandListener.action(messageId, actionMessage, out);
    }

    @Override
    public void customOperation(final String messageId, final String requestBody, final PrintWriter out) {
        if(logger.isTraceEnabled()){
            logger.trace("custom operation called... with request body {} ", requestBody);
        }
        this.commandListener.customOperation(messageId, requestBody, out);
    }

    @Override
    public void customOperation(final String messageId, final String requestBody, final boolean returnResponse, final PrintWriter out) {
        logger.trace("custom operation called with requestBody {} and Response required {}", requestBody, returnResponse);
        this.commandListener.customOperation(messageId, requestBody, returnResponse, out);

    }

    @Override
    public void copyConfig(final String messageId, final String source, final String target, final PrintWriter out) {
        logger.trace("copy config operation called... with source {} , target {} ", source, target);
        this.commandListener.copyConfig(messageId, source, target, out);

    }

    protected abstract void onExit(int exitValue, String exitMessage);

    protected abstract void onExit(int exitValue);

    NetconfResponse connectNetconfManager() throws NetconfManagerException {
        NetconfResponse connectionResponse = commandListener.getNetconfManager().connect();
        remoteSessionId = getNetconfManager().getSessionId();
        METRICS_HANDLER.setSouthboundSession(sessionId, remoteSessionId);
        logger.debug("Netconf Client remote session id: {}", remoteSessionId);
        killableSessionToRemoteMap.put(sessionId, remoteSessionId);
        return connectionResponse;
    }

    @Override
    public void getSchema(String messageId, String identifier, PrintWriter out) {
        this.commandListener.getSchema(messageId, identifier, out);
    }
    
    protected abstract void shutdown();
    
}
