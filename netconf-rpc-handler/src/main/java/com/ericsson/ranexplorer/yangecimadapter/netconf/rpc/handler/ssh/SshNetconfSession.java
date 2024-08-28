/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.*;

import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.filter.SubTreeFilter;
import com.ericsson.ranexplorer.yangecimadapter.netconf.client.NetconfManagerBuilder;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.Configuration;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.SshConfiguration;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.KillSessionException;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server.NetconfSession;

public class SshNetconfSession extends NetconfSession {
    private static final Logger logger = LoggerFactory.getLogger(SshNetconfSession.class);

    private static final Map<Integer, Killable> killableSessions = new ConcurrentHashMap<>();
    private static final int MINIMUM_IDLE_TIMEOUT_DIFF = 100;
    private static final int IDLE_TIMEOUT_DIFF = 10000;
    private final ExitCallback callback;
    private final String user;
    private final ServerSession session;
    private final ScheduledExecutorService timerExecutor = Executors.newSingleThreadScheduledExecutor(new TimeoutResetThreadFactory());

    public SshNetconfSession(final ExitCallback callback, final InputStream in, final OutputStream out,
                             final CommandListener commandListener, final Configuration configuration,
                             final String user, final ServerSession session) {
        super(in, out, commandListener, configuration);
        this.callback = callback;
        this.user = user;
        this.session = session;
    }

    @Override
    public void hello(final int sessionId, final PrintWriter out) {
        logger.info("calling hello in SshNetconfSession");
        super.hello(sessionId, out);
        killableSessions.put(sessionId, this);
    }

    @Override
    public void killSession(final String messageId, final int sessionId, final String southSessionId, final PrintWriter out) {
        try {
            super.killSession(messageId, sessionId, southSessionId, out);
            if (sessionId != this.sessionId) {
                final Killable otherSession = killableSessions.remove(sessionId);
                if (otherSession != null) {
                    otherSession.kill();
                }
            }
        }catch (KillSessionException e){
            logger.error("Failed to kill session {}", sessionId, e);
        }
    }

    public void destroy() {
        this.kill();
        killableSessions.remove(this.sessionId);
    }

    @Override
    public Callable<Boolean> createSubscription(final String messageId, final String stream, final Filter filter,
                                                final String startTime, final String stopTime, final PrintWriter out) {
        Callable<Boolean> notificationSender = super.createSubscription(messageId, stream, filter, startTime, stopTime, out);
        long socketTimeout = configuration.getSocketTimeout();
        long period;
        if(socketTimeout > 0) {
            if (socketTimeout <  2 * IDLE_TIMEOUT_DIFF) {
                logger.warn("Configured idleTimeout is less than {} ms. This leads to overhead in keeping session alive in subscribed state", 2 * IDLE_TIMEOUT_DIFF);
                period = socketTimeout - MINIMUM_IDLE_TIMEOUT_DIFF;
            } else {
                period = socketTimeout - IDLE_TIMEOUT_DIFF;
            }
            timerExecutor.scheduleAtFixedRate(new TimeoutReset(), period, period, TimeUnit.MILLISECONDS);
        }
        return notificationSender;
    }

    @Override
    protected void onExit(final int exitValue, final String exitMessage) {
        callback.onExit(exitValue, exitMessage);
    }

    @Override
    protected void onExit(final int exitValue) {
        callback.onExit(exitValue);
    }

    @Override
    public NetconfManager getNetconfManager() {
        return commandListener.getNetconfManager();
    }

    @Override
    public void setNetconfManager(NetconfManager netconfManager) {
        commandListener.setNetconfManager(netconfManager);
    }

    @Override
    public void setSubscribedState(boolean subscribed) {
        commandListener.setSubscribedState(subscribed);
    }

    @Override
    public void setSessionId(int sessionId) {
        commandListener.setSessionId(sessionId);
    }

    @Override
    protected NetconfManager buildManager(String nodeAddress, int nodePort) throws NetconfManagerException {
        NetconfManagerBuilder nmr = new NetconfManagerBuilder("SSH", sessionId);
        return nmr.hostName(nodeAddress).port(nodePort).idleConnectionTimeoutSeconds((int)configuration.getSocketTimeout())
                .credentials(getUser(), getPassword(getUser())).localAddress(configuration.getNodeBindAddress())
                .stdCapabilities().build();
    }

    private String getPassword(String user) {
        Map<String, String> users = ((SshConfiguration)configuration).getUsers();
        if(users.containsKey(user)) {
            return users.get(user);
        }
        throw new NetconfServerException("User not found for Node login!");
    }

    private String getUser() {
        return this.user;
    }

    @Override
    protected int getDefaultPort() {
        return 22;
    }

    private class TimeoutReset implements Runnable {

        @Override
        public void run() {
            try {
                logger.debug("Resetting the idle Timeout for session [{}] in subscribed state", sessionId);
                session.resetIdleTimeout();
                getNetconfManager().get(new SubTreeFilter(""));
            } catch (NetconfManagerException exception) {
                logger.error("Empty GET request (Netconf ping) to node failed for session id [{}] in subscribed state. Shutting down the session.", sessionId, exception);
                //no reconnection in subscribed state
                timerExecutor.shutdown();
                destroy();
            } catch (final Exception exception) {
                logger.error("Exception thrown while resetting the idle timeout. Ignoring the exception and continuing.", exception);
            } catch (final Throwable throwable) { //NOSONAR - Need to keep session alive when in subscribed state
                logger.error("Throwable thrown while resetting the idle timeout. Ignoring the throwable and continuing.", throwable);
            }
        }
    }

    private class TimeoutResetThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SubscribedSessionIdleTimeoutResetThread-" + sessionId);
        }

    }

    @Override
    protected void shutdown() {
        timerExecutor.shutdownNow();
        try {
            timerExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            logger.warn("Thread was interrupted while waiting for executor to shutdown");
            Thread.currentThread().interrupt();
        }
    }

}
