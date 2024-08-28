/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server;

import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.NetconfConnectionStatus;
import com.ericsson.oss.mediation.util.netconf.api.NetconfResponse;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.DefaultOperation;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.ErrorOption;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.TestOption;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerConnectionException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.AdapterListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

/**
 * Utility class for handling NetconfManager reconnect attempts.
 * Can be updated for reconnect attempt counter if necessary
 */
class NetconfSessionUtility {
    private static final Logger logger = LoggerFactory.getLogger(NetconfSession.class);

    private CommandListener commandListener;
    private NetconfSession netconfSession;

    NetconfSessionUtility(CommandListener commandListener, NetconfSession netconfSession) {
        this.commandListener = commandListener;
        this.netconfSession = netconfSession;
    }

    void retryGetOperation(final String messageId, final Filter filter, final PrintWriter out) {
        try{
            reconnectNetconfManagerIfDisconnected();
            this.commandListener.get(messageId, filter, out);
        }catch (AdapterListenerException ex){
            throw new NetconfServerConnectionException(ex);
        }
    }

    void retryGetConfigOperation(String messageId, Datastore datastore, Filter filter, PrintWriter out) {
        try{
            reconnectNetconfManagerIfDisconnected();
            this.commandListener.getConfig(messageId, datastore, filter, out);
        }catch (AdapterListenerException ex){
            throw new NetconfServerConnectionException(ex);
        }
    }

    void retryEditConfigOperation(String messageId, Datastore datastore,
                                          DefaultOperation defaultOperation, ErrorOption errorOption,
                                          TestOption testOption, String config, PrintWriter out) {
        try{
            reconnectNetconfManagerIfDisconnected();
            this.commandListener.editConfig(messageId, datastore, defaultOperation, errorOption, testOption, config, out);
        }catch (AdapterListenerException ex){
            throw new NetconfServerConnectionException(ex);
        }

    }

    Callable<Boolean> retryCreateSubscriptionOperation(String messageId, String stream, Filter filter, String startTime, String stopTime, PrintWriter out) {
        Callable<Boolean>notificationSender;
        try{
            notificationSender = netconfSession.createNotificationSender(messageId, stream, filter, startTime, stopTime, out);
        }catch (AdapterListenerException e){
            throw new NetconfServerConnectionException(e);
        }
        return notificationSender;
    }

    void reconnectNetconfManagerIfDisconnected(){
        if(commandListener.getNetconfManager().getStatus() != NetconfConnectionStatus.CONNECTED){
            reconnectNetconfManager();
        }
    }

    private void reconnectNetconfManager(){
        try {
            NetconfResponse connectionResponse = netconfSession.connectNetconfManager();
            logger.info("Netconf Client connection re-established: {}", connectionResponse);
        } catch (NetconfManagerException e) {
            throw new NetconfServerConnectionException(e);
        }
    }
}
