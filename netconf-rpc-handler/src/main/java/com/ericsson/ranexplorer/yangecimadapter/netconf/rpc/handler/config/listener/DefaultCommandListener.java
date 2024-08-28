/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;

import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.oss.mediation.util.netconf.api.NetconfResponse;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.DefaultOperation;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.ErrorOption;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.TestOption;
import com.ericsson.oss.mediation.util.netconf.api.error.Error;
import com.ericsson.oss.mediation.util.netconf.api.error.ErrorSeverity;
import com.ericsson.oss.mediation.util.netconf.api.error.ErrorTag;
import com.ericsson.oss.mediation.util.netconf.api.error.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@XmlRootElement(name = "default-listener")
public class DefaultCommandListener implements CommandListener {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCommandListener.class);
    private static final String VERSION_1_0_END_TAG = "]]>]]>";
    private static final String RPC_REPLY_START_TAG = "<rpc-reply message-id=\"%s\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">";
    private static final String RPC_REPLY_END_TAG ="</rpc-reply>";
    private static final String RPC_VERSION_1_0_TAG ="xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">";

    protected static final int VERSION_1_0 = 10;
    protected static final int VERSION_1_1 = 11;
    protected AtomicInteger version = new AtomicInteger(VERSION_1_0);
    protected AtomicBoolean subscribed = new AtomicBoolean(false);

    @Override
    public void closeSession(final String messageId, final PrintWriter out) {
        actionOkReply(messageId, out);
    }

    @Override
    public void clientHello(final List<String> capabilities, final PrintWriter out) {
        //no default implementation
    }

    @Override
    public void hello(final int sessionId, final PrintWriter out) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<hello " + RPC_VERSION_1_0_TAG);
        out.println("\t<capabilities>");
        out.println(getCapabilityTree("urn:ietf:params:xml:ns:netconf:base:1.0"));
        out.println("\t</capabilities>");
        out.print("\t<session-id>");
        out.print(sessionId);
        out.println("</session-id>");
        out.println("</hello>");
        out.println(VERSION_1_0_END_TAG);
    }

    @Override
    public void get(final String messageId, final Filter filter, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("get"), out);
    }

    @Override
    public void editConfig(final String messageId, final Datastore source, final DefaultOperation defaultOperation, final ErrorOption errorOption,
                           final TestOption testOption, final String config, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("editConfig"), out);
    }

    @Override
    public void getConfig(final String messageId, final Datastore source, final Filter filter, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("getConfig"), out);
    }

    @Override
    public void killSession(final String messageId, final int sessionId, final String southSessionId, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("killSession"), out);
    }

    @Override
    public void sendError(final String messageId, final String rpcError, final PrintWriter out) {

        if (rpcError == null || rpcError.isEmpty()) {
            out.print(String.format(RPC_REPLY_START_TAG, messageId));
            out.println("\t<rpc-error>");
            out.println("\t\t<error-type>application</error-type>");
            out.println("\t\t<error-tag>operation-failed</error-tag>");
            out.println("\t\t<error-severity>error</error-severity>");
            out.println("\t\t<error-message>");
            out.println("\t\t\tUnexpected error: {failed_to_parse_xml,");
            out.println("\t\t\t{fatal,}");
            out.println("\t\t</error-message>");
            out.println("\t</rpc-error>");
            out.println(RPC_REPLY_END_TAG);
            out.println(VERSION_1_0_END_TAG);
        } else {
            out.print(rpcError);
            out.println(VERSION_1_0_END_TAG);
        }

    }

    @Override
    public Callable<Boolean> createSubscription(final String messageId, final String stream, final Filter filter, final String startTime,
                                                final String stopTime, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("createSubscription"), out);
        return null;
    }

    @Override
    public void lock(final String messageId, final String sessionid, final Datastore target, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("lock"), out);
    }

    @Override
    public void unlock(final String messageId, final Datastore target, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("unlock"), out);
    }

    @Override
    public void validate(final String messageId, final Datastore source, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("validate"), out);
    }

    @Override
    public void commit(final String messageId, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("commit"), out);
    }

    protected String getCapabilityTree(final String capability) {
        return String.format("\t\t<capability>%s</capability>", capability);
    }

    @Override
    public void action(final String messageId, final String actionMessage, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("action"), out);
    }

    protected void actionOkReply(final String messageId, final PrintWriter out) {
        String s = /*"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +*/
                String.format(RPC_REPLY_START_TAG, messageId) +
                        "\n<ok/>\n" +
                        RPC_REPLY_END_TAG;

        LOG.debug("Sending ok message {}\n", s);
        sendMessage(s, out);
    }

    protected void actionGetReply(final String messageId, String reply, final PrintWriter out) {
        String s = String.format(RPC_REPLY_START_TAG, messageId) +
                "\n<data>\n" + reply + "</data>\n" +
                RPC_REPLY_END_TAG;
        LOG.debug("Reply to get/get-config is \n{}", s);
        sendMessage(s, out);
    }

    protected void actionGetSchemaReply(final String messageId, String reply, final PrintWriter out) {
        String s = String.format(RPC_REPLY_START_TAG, messageId) +
                "<data xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\"><![CDATA[" + reply + "]]></data>\n" + RPC_REPLY_END_TAG;
        LOG.debug("The get-schema reply is \n{}", s);
        sendMessage(s, out);

    }

    protected void sendMessage(String message, final PrintWriter out){
        //todo review, netconf-console fails to parse response when xml version added.//NOSONAR
        if(version.get() == VERSION_1_1){
            sendMessageVersion11(message, out);
        }else {
            sendMessageVersion10(message, out);
        }
        out.flush();
    }

    private void sendMessageVersion10(final String message, final PrintWriter out){
        out.print(message);
        out.print(VERSION_1_0_END_TAG);
    }

    private void sendMessageVersion11(final String message, final PrintWriter out){
        out.print("\n#" + message.getBytes().length + "\n");
        out.print(message);
        out.print("\n##\n");
    }

    protected void writeErrorMessage(String messageId, NetconfResponse result, final PrintWriter out) {
        LOG.error("Netconf response error message: {}", result.getErrorMessage());
        Error error;
        if(result.getErrors() == null || result.getErrors().isEmpty()){
            error = new Error();
            error.setErrortype(ErrorType.application);
            error.setErrorTag(ErrorTag.OPERATION_FAILED);
            error.setErrorSeverity(ErrorSeverity.error);
            error.setErrorMessage(result.getErrorMessage());
        }else {
            error = result.getErrors().get(0);
            if (error.getErrorMessage() == null || error.getErrorMessage().isEmpty()) {
                error.setErrorMessage(result.getErrorMessage());
            }
        }
        writeErrorMessage(messageId, error, out);
    }

    protected void writeErrorMessage(String messageId, Error error, final PrintWriter out) {
        String msg = String.format(RPC_REPLY_START_TAG, messageId) + "\n" +
                "<rpc-error> \n" +
                "<error-type>" + error.getErrortype() + "</error-type> \n" +
                "<error-tag>" + reformatErrorTag(error.getErrorTag().toString()) + "</error-tag> \n" +
                "<error-severity>" + error.getErrorSeverity() + "</error-severity> \n";

        if (error.getErrorMessage() != null && !error.getErrorMessage().isEmpty()) {
            msg = msg + " <error-message xml:lang=\"en\">" + error.getErrorMessage() + "</error-message> \n";
        }

        msg = msg + " </rpc-error> \n" + RPC_REPLY_END_TAG;

        LOG.debug("Error response is \n{}", msg);
        sendMessage(msg, out);
    }

    private Error getUnsupportedMethodError(String method){
        Error error = new Error();
        error.setErrortype(ErrorType.application);
        error.setErrorTag(ErrorTag.OPERATION_FAILED);
        error.setErrorSeverity(ErrorSeverity.error);
        error.setErrorMessage("Unsupported operation: " + method);
        return error;
    }

    private String reformatErrorTag(String errorTagString) {
        return errorTagString.toLowerCase().replaceAll("_", "-");
    }

    @Override
    public void discardChanges(final String messageId, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("discardChanges"), out);
    }

    @Override
    public void customOperation(final String messageId, final String requestBody, final PrintWriter out) {
        customOperation(messageId, requestBody, true, out);

    }

    @Override
    public void customOperation(final String messageId, final String requestBody, final boolean returnResponse, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("customOperation"), out);

    }

    @Override
    public void copyConfig(final String messageId, final String source, final String target, final PrintWriter out) {
        writeErrorMessage(messageId, getUnsupportedMethodError("copyConfig"), out);
    }

    @Override
    public NetconfManager getNetconfManager() {
        throw new UnsupportedOperationException("Cannot call getNetconfManager() on " +
                "DefaultCommandListener. DefaultCommandListener does not contain a NetconfManager");
    }

    @Override
    public void setNetconfManager(NetconfManager netconfManager) {
        throw new UnsupportedOperationException("Cannot call setNetconfManager() on " +
                "DefaultCommandListener. DefaultCommandListener does not contain a NetconfManager");
    }

    @Override
    public void getSchema(String messageId, String identifier, PrintWriter out) {
        actionOkReply(messageId, out);
    }

    @Override
    public void setSubscribedState(boolean subscribed) {
        this.subscribed.set(subscribed);
    }

    public void setSessionId(int sessionId) {
        LOG.warn("Requesting to set the sessionId to {} on the DefaultCommandListener. SessionId is not used in the DefaultCommandListener.", sessionId);
    }
}
