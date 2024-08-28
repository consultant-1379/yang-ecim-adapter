/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

import com.ericsson.oss.mediation.util.netconf.api.editconfig.DefaultOperation;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.ErrorOption;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.TestOption;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;

public interface CommandListener {

    void closeSession(String messageId, PrintWriter out);

    void hello(int sessionId, PrintWriter out);

    void clientHello(List<String> capabilities, PrintWriter out);

    void action(String messageId, String actionMessage, PrintWriter out);

    void get(String messageId, Filter filter, PrintWriter out);

    void getConfig(String messageId, Datastore source, Filter filter, PrintWriter out);

    void lock(String messageId, String sessionid, Datastore target, PrintWriter out);

    void unlock(String messageId, Datastore target, PrintWriter out);

    void validate(String messageId, Datastore source, PrintWriter out);

    void commit(String messageId, PrintWriter out);

    void discardChanges(String messageId, PrintWriter out);

    void editConfig(String messageId, Datastore source, DefaultOperation defaultOperation, ErrorOption errorOption, TestOption testOption,
                    String config, PrintWriter out);

    void killSession(String messageId, int northSessionId, String southSessionId, PrintWriter out);

    Callable<Boolean> createSubscription(String messageId, String stream, Filter filter, String startTime, String stopTime, PrintWriter out);

    void sendError(String messageId, String rpcError, PrintWriter out);

    void customOperation(String messageId, String requestBody, PrintWriter out);

    void customOperation(String messageId, String requestBody, boolean returnResponse, PrintWriter out);

    void copyConfig(String messageId, String source, String target, PrintWriter out);

    NetconfManager getNetconfManager();

    void setNetconfManager(NetconfManager netconfManager);

    void getSchema(String messageId, String identifier, PrintWriter out);

    void setSubscribedState(boolean subscribed);

    void setSessionId(int sessionId);
}
