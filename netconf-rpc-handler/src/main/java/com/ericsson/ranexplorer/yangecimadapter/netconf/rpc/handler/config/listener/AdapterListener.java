/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.NetconfErrorHandler.*;
import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.constants.AdapterConstants.*;

import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.ericsson.oss.mediation.transport.api.TransportManager;
import com.ericsson.oss.mediation.util.netconf.api.*;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.*;
import com.ericsson.oss.mediation.util.netconf.api.error.Error;
import com.ericsson.oss.mediation.util.netconf.api.error.ErrorTag;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.filter.SubTreeFilter;
import com.ericsson.oss.mediation.util.netconf.manager.NetconfManagerImpl;
import com.ericsson.ranexplorer.yangecimadapter.common.services.netconf.capabilities.CapabilityService;
import com.ericsson.ranexplorer.yangecimadapter.common.services.util.AdapterUtils;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.*;
import com.ericsson.ranexplorer.yangecimadapter.netconf.client.NotificationQueueProvider;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterProcessor;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterProcessorFactory;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.*;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformerFactory;

public class AdapterListener extends DefaultCommandListener {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterListener.class);
    private static final String XPATH = "xpath";
    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;
    public static final String MANAGED_ELEMENT_FILTER =
            "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\"><managedElementId/></ManagedElement>";
    private static final String VERSION_1_DOT_0_TERMINATOR = "]]>]]>";

    private final XsltTransformer ecimToYangTransformer = XsltTransformerFactory.newEcimToYangTransformer();
    private final XsltTransformer notificationTransformer = XsltTransformerFactory.newNotificationTransformer();
    private final XsltTransformer mergeResultsDefaultFilterTransformer = XsltTransformerFactory.newMergeResultsDefaultFilterTransformer();
    private NetconfManager netconfManager;
    private String managedElementId = "1";
    private int sessionId;

    @Override
    public void clientHello(final List<String> capabilities, final PrintWriter out) {
        METRICS_HANDLER.markReset(sessionId);
        for(final String capability : capabilities){
            LOG.trace("Capability string is {}", capability);
            if(capability.contains("urn:ietf:params:netconf:base:1.1")){
                version.set(VERSION_1_1);
                LOG.debug("Netconf version set to 1.1");
                break;
            }
        }
    }

    @Override
    public void hello(final int sessionId, final PrintWriter out) {
        final StringBuilder capabilities = new StringBuilder();
        capabilities.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">").append("\t<capabilities>");
        CapabilityService capabilityService = new CapabilityService();
        capabilities.append(capabilityService.getCapabilitiesAsXML("capabilities.to.client") );
        capabilities.append("\t</capabilities>\n").append("\t<session-id>").
                append(sessionId).append("</session-id>\n").append("</hello>").append(VERSION_1_DOT_0_TERMINATOR);
        out.print(capabilities);
        out.flush();
        setManagedElementId();
    }

    private void setManagedElementId(){
        try {
            final NetconfResponse netconfResponse = getNetconfManager().get(new SubTreeFilter(MANAGED_ELEMENT_FILTER));
            final String netconfResponseData = netconfResponse.getData();
            if(netconfResponseData.contains("managedElementId")){
                managedElementId = extractManagedElementId(netconfResponseData);
            }
        } catch (NetconfManagerException exception) {
            LOG.error("NetconfManagerException occurred during setting of managedElementId", exception);
            throw new AdapterListenerException(exception);
        }
    }

    private String extractManagedElementId(final String netconfResponse) {
        try{
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource inputSource = new InputSource(new StringReader(netconfResponse));
            String result = xpath.evaluate("/*[local-name()=\"ManagedElement\"]/*[local-name()=\"managedElementId\"]", inputSource);
            if(!StringUtils.isEmpty(managedElementId)){
                return result;
            }
        }catch ( XPathExpressionException exception){
            LOG.error("XPathExpressionException occurred while getting ManagedElementId ", exception);
        }
        LOG.info("ManagedElementId is {} ", managedElementId);
        return "1";
    }

    @Override
    public void editConfig(final String messageId, final Datastore source,
                           final DefaultOperation operation, final ErrorOption error,
                           final TestOption test, final String yangXml, final PrintWriter out) {
        LOG.debug("Handling edit-config message");
        LOG.trace("Datastore is {}", source);
        LOG.trace("DefaultOperation is {}", operation);
        LOG.trace("ErrorOption is {}", error);
        LOG.trace("TestOption is {}", test);

        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.EDIT_CONFIG);
        if (source != Datastore.RUNNING) {
            final Error dataStoreError = createUnsupportedDataStoreError(source.asParameter());
            LOG.error(dataStoreError.getErrorMessage());
            writeErrorMessage(messageId, dataStoreError, out);
            return;
        }

        try {
            METRICS_HANDLER.markStart(sessionId,MessageState.YANG_TO_ECIM );
            //transformWithDummyRootWrapper to avoid multiple root elements in transformation
            final String ecimXml = transformFilterToEcim(yangXml, "default");
            METRICS_HANDLER.markEnd(sessionId,MessageState.YANG_TO_ECIM );
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);

            final NetconfResponse response = netconfManager.editConfig(Datastore.RUNNING, operation,
                    error, test, ecimXml);
            METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
            LOG.debug("Node returned: {}", response.getData());

            if(response.isError()){
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                writeErrorMessage(messageId, response, out);
                METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
            }else {
                actionOkReply(messageId, out);
            }
        }catch (final TransformerException exception){
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error(XML_TRANSFORM_ERROR_MSG, exception);
            writeErrorMessage(messageId, createOperationFailedError(XML_TRANSFORM_ERROR_MSG), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        } catch (final NetconfManagerException exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error("NetconfManagerException occurred during editConfig request!", exception);
            throw new AdapterListenerException(exception);
        }
    }

    @Override
    public void get(final String messageId, final Filter filter, final PrintWriter out) {
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.GET);
        executeGet(messageId, filter, out, GetOperation.GET);
    }

    @Override
    public void getConfig(final String messageId, final Datastore source, final Filter filter, final PrintWriter out) {
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.GET_CONFIG);
        if (source != Datastore.RUNNING) {
            METRICS_HANDLER.markEnd(sessionId,MessageState.MESSAGE_REQUEST);
            METRICS_HANDLER.markStart(sessionId,MessageState.MESSAGE_RESPONSE);
            METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
            final Error error = createUnsupportedDataStoreError(source.asParameter());
            LOG.error(error.getErrorMessage());
            writeErrorMessage(messageId, error, out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
            return;
        }

        executeGet(messageId, filter, out, GetOperation.GET_CONFIG);
    }

    private void executeGet(final String messageId, Filter filter, final PrintWriter out, final GetOperation getOperation) {
        try {
            if (isXpathFilter(filter)) {
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                final Error error = createXpathFilterNotSupportedError();
                LOG.error(error.getErrorMessage());
                writeErrorMessage(messageId, error, out);
                METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
                return;
            }

            METRICS_HANDLER.markStart(sessionId, MessageState.FILTER_PROCESS);
            final FilterProcessor filterProcessor = getOperation.getFilterProcessor(filter, mergeResultsDefaultFilterTransformer);
            String filterString = filterProcessor.getFilterStringApplicableToNode();
            String filterStringToUse = filterProcessor.shouldTransform() ? transformFilterToEcim(filterString, "default") : filterString;
            SubTreeFilter subTreeFilter = new SubTreeFilter(filterStringToUse);
            METRICS_HANDLER.markEnd(sessionId, MessageState.FILTER_PROCESS);
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);

            final NetconfResponse netconfResponse = getOperation.execute(netconfManager, subTreeFilter);
            METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
            METRICS_HANDLER.markStart(sessionId, MessageState.RESPONSE_POST_PROCESS);
            final String netconfResponseData = getCleanedNetconfResponseData(netconfResponse);
            LOG.debug("Node Returned: {}", netconfResponseData);

            if (netconfResponse.isError()) {
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                writeErrorMessage(messageId, netconfResponse, out);
                METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
                METRICS_HANDLER.markEnd(sessionId, MessageState.RESPONSE_POST_PROCESS);
                return;
            }

            final String transformedXml = isEmpty(netconfResponseData)? "": ecimToYangTransformer.transform(netconfResponseData);
            LOG.debug("transformedXml response: {}", transformedXml);
            String yangXml = filterProcessor.postProcess(transformedXml);
            METRICS_HANDLER.markEnd(sessionId, MessageState.RESPONSE_POST_PROCESS);
            actionGetReply(messageId, yangXml, out);
        } catch (final TransformerException exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error(XML_TRANSFORM_ERROR_MSG, exception);
            writeErrorMessage(messageId, createOperationFailedError(XML_TRANSFORM_ERROR_MSG), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        } catch (FilterProcessingException exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            final String msg = "Unable to parse the xml data in the filter";
            LOG.error(msg, exception);
            writeErrorMessage(messageId, createOperationFailedError(msg), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        } catch (XmlFileReaderException exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            final String msg = exception.getMessage();
            LOG.error(msg, exception);
            writeErrorMessage(messageId, createOperationFailedError(msg), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        }catch (NetconfManagerException e) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error("NetconfManagerException occurred during get request!", e);
            throw new AdapterListenerException(e);
        }
    }

    private String getCleanedNetconfResponseData(final NetconfResponse netconfResponse) {
        String netconfResponseData = netconfResponse.getData();
        if("<data".equals(netconfResponseData.trim())){
            // Workaround for an issue in Netconf Manager where empty data can be returned as "<data"
            netconfResponseData = "";
        }
        return netconfResponseData;
    }

    private boolean isEmpty(final String data){
        return data.trim().isEmpty();
    }

    private boolean isXpathFilter(final Filter filter) {
        return filter != null && filter.getType() != null && XPATH.equals(filter.getType().trim());
    }

    private String transformFilterToEcim(final String yangFilter, final String templateType) throws TransformerException{
        //transformWithDummyRootWrapper to avoid multiple root elements in transformation
        final  XsltTransformer yangToEcimTransformer = XsltTransformerFactory.newYangToEcimTransformer(managedElementId, templateType);
        return yangToEcimTransformer.transformWithDummyRootWrapper(yangFilter.trim());
    }

    @Override
    public void killSession(final String messageId, final int clientSessionIdToBeKilled, final String southSessionId, final PrintWriter out) {
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.KILL_SESSION);
        try {
            if(StringUtils.isEmpty(southSessionId)){
                final String msg = "Session with id " + clientSessionIdToBeKilled +" does not exist";
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                writeErrorMessage(messageId, createOperationFailedError(msg), out);
                throw new KillSessionException("Invalid session id!");
            }
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
            NetconfResponse response = getNetconfManager().killSession(southSessionId);
            METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
            if (response.isError()) {
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                writeErrorMessage(messageId, response, out);
                METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
                throw new KillSessionException(response.getErrorMessage());
            }else {
                actionOkReply(messageId, out);
            }
        } catch (NetconfManagerException e) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.warn("Exception while trying to kill session with id [{}] on the node. The corresponding session id to the client is [{}]", southSessionId, clientSessionIdToBeKilled, e);
            final String msg = e.getMessage();
            writeErrorMessage(messageId, createOperationFailedError(msg), out);
            throw new KillSessionException(e);
        }

    }

    @Override
    public void closeSession(final String messageId, final PrintWriter out) {
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.CLOSE_SESSION);
        try {
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
            if(getNetconfManager().getStatus() == NetconfConnectionStatus.CONNECTED) {
                getNetconfManager().disconnect();
            }
        } catch (final NetconfManagerException e) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error("NetconfManagerException occurred: {}", e.getMessage(), e);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        }
        METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
        actionOkReply(messageId, out);
    }

    @Override
    public NetconfManager getNetconfManager() {
        return netconfManager;
    }

    @Override
    public void setNetconfManager(final NetconfManager netconfManager) {
        this.netconfManager = netconfManager;
    }

    @Override
    public void setSessionId(final int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public Callable<Boolean> createSubscription(final String messageId, final String stream, final Filter filter, final String startTime,
                                                final String stopTime, final PrintWriter out) {
        NotificationSender notificationSender = null;
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.CREATE_SUBSCRIPTION);
        try {
            METRICS_HANDLER.markStart(sessionId, MessageState.FILTER_PROCESS);
            final FilterProcessor filterProcessor = FilterProcessorFactory.getFilterProcessorCreateSubscription(filter);
            String filterString = filterProcessor.getFilterStringApplicableToNode();
            LOG.debug("FilterString : {}", filterString);
            String filterStringToUse = transformFilterToEcim(filterString, "subscription");
            SubTreeFilter subTreeFilter = new SubTreeFilter(filterStringToUse);
            METRICS_HANDLER.markEnd(sessionId, MessageState.FILTER_PROCESS);
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
            LOG.trace("creating subscription ...");
            final NetconfResponse result = getNetconfManager().createSubscription(stream, subTreeFilter, startTime, stopTime);
            METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
            if (result.isError()) {
                METRICS_HANDLER.markStart(sessionId, MessageState.ERROR);
                writeErrorMessage(messageId, result, out);
                METRICS_HANDLER.markEnd(sessionId, MessageState.ERROR);
                throw new NetconfServerException("Wasn't able to subscribe for notifications with NetconfManager!");
            } else {
                actionOkReply(messageId, out);
                TransportManager manager = ((NetconfManagerImpl) getNetconfManager()).getTransportManager();
                notificationSender = new NotificationSender(((NotificationQueueProvider)manager).getNotificationQueue(), out);
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Check Create Subscription : {}", result.getData());
                }
            }

        } catch (final NetconfManagerException exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error("NetconfManagerException occurred: {}", exception.getMessage(), exception);
            throw new AdapterListenerException(exception);
        } catch (final TransformerException exception){
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            LOG.error(XML_TRANSFORM_ERROR_MSG, exception);
            writeErrorMessage(messageId, createOperationFailedError(XML_TRANSFORM_ERROR_MSG), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        }
        return notificationSender;
    }

    @Override
    public void getSchema(String messageId, String identifier, PrintWriter out) {
        LOG.info("get-schema is called with identifier [{}]", identifier);
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.GET_SCHEMA);
        String yangFile = "/" + identifier + ".yang";
        try {
            METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
            METRICS_HANDLER.markStart(sessionId,MessageState.MESSAGE_RESPONSE);
            actionGetSchemaReply(messageId, AdapterUtils.readFileToString(yangFile), out);
        } catch (Exception exception) {
            METRICS_HANDLER.markStart(sessionId, MessageState.EXCEPTION);
            String msg = "Failed to process schema file";
            LOG.error("Failed to process schema file: {}", exception.getMessage(), exception);
            writeErrorMessage(messageId, createError(msg, ErrorTag.BAD_ATTRIBUTE), out);
            METRICS_HANDLER.markEnd(sessionId, MessageState.EXCEPTION);
        }
    }

    @Override
    public void lock(String messageId, String sessionid, Datastore target, PrintWriter out) {
        // Lock is not supported but appears to be needed by OpenDaylight so simply replying ok
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.LOCK);
        METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
        METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
        actionOkReply(messageId, out);
    }

    @Override
    public void unlock(String messageId, Datastore target, PrintWriter out) {
        // unlock is not supported but appears to be needed by OpenDaylight so simply replying ok
        setMessageIdAndOperationTypeinMetrics(messageId, RpcOperation.UNLOCK);
        METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_REQUEST);
        METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_RESPONSE);
        actionOkReply(messageId, out);
    }

    private void setMessageIdAndOperationTypeinMetrics(final String messageId, final RpcOperation rpcOperation) {
        METRICS_HANDLER.setMessageId(sessionId, messageId);
        METRICS_HANDLER.setRpcOperation(sessionId, rpcOperation);
    }

    protected class NotificationSender implements Callable<Boolean>{
        final PrintWriter out;
        final  Queue<String> notificationQueue;

        public NotificationSender(Queue<String> notificationQueue, PrintWriter out) {
            this.out = out;
            this.notificationQueue = notificationQueue;
        }

        @Override
        public Boolean call() throws InterruptedException {
            while (subscribed.get()) {
                String data = notificationQueue.poll();
                if(data == null){
                    Thread.sleep(10);
                    continue;
                }

                if (data.contains("notification")) {
                    try {
                        data = transformNotification(data);
                        sendMessage(data, out);
                        METRICS_HANDLER.notificationSent(sessionId);
                        LOG.debug("Notification sent: [{}]", data);
                    } catch (TransformerException exception) {
                        METRICS_HANDLER.markEnd(sessionId, NotificationState.TRANSLATION);
                        METRICS_HANDLER.dropNotificationRecord(sessionId);
                        LOG.error("Not sending notification as failed to transform the notification \n{}\n", data, exception);
                    }

                }else {
                    METRICS_HANDLER.dropNotificationRecord(sessionId);
                    LOG.warn("Message Received not notification: {}",  data);
                }
                if (data.contains("notificationComplete")) {
                    return true;
                }
            }
            return true;
        }

        private String transformNotification(String data) throws TransformerException {
            METRICS_HANDLER.markStart(sessionId, NotificationState.TRANSLATION);
            LOG.debug("Message Received: {}",  data);
            String notification = data.trim();
            if (notification.endsWith(VERSION_1_DOT_0_TERMINATOR)) {
                notification = notification.substring(0, notification.length() - VERSION_1_DOT_0_TERMINATOR.length());
            }
            notification = notificationTransformer.transform(notification);
            METRICS_HANDLER.markEnd(sessionId, NotificationState.TRANSLATION);
            return notification;
        }
    }
}
