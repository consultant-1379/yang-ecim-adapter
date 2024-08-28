/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.client;

import com.ericsson.oss.mediation.transport.api.*;
import com.ericsson.oss.mediation.transport.api.exception.TransportException;
import com.ericsson.oss.mediation.transport.api.manager.TransportManagerError;
import com.ericsson.oss.mediation.transport.api.provider.TransportProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class is decorating  transport manager created by ENM mediation,
 * where TransportProviderListener is supported adding queued message processing
 */
public class AdapterTransportManager implements TransportManager, NotificationQueueProvider {

    private static final Logger logger = LoggerFactory.getLogger(AdapterTransportManager.class);
    private final ConcurrentLinkedQueue<String> netconfReplyQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> notificationQueue = new ConcurrentLinkedQueue<>();
    private final TransportManager transportManager;
    private static final int WAITING_MESSAGE_ID = -1;
    private final AtomicLong expectedMessageId = new AtomicLong(WAITING_MESSAGE_ID);
    private final AdapterTransportProviderListener adapterTransportProviderListener;
    private Lock lock = new ReentrantLock();
    private Condition inProcess = lock.newCondition();

    public AdapterTransportManager(final TransportManager transportManager, final int sessionId) {
        this.transportManager = transportManager;
        adapterTransportProviderListener = new AdapterTransportProviderListener(sessionId, netconfReplyQueue, notificationQueue);
    }

    /**
     * @return the transportProvider
     */
    @Override
    public TransportProvider getTransportProvider() {
        return transportManager.getTransportProvider();
    }

    @Override
    public void openConnection() throws TransportException {
        TransportProvider provider = getTransportProvider();
        if (provider == null) {
            throw new TransportException(TransportManagerError.PROVIDER_NOT_AVAILABLE, TransportSeverity.FATAL);
        } else {
            if (!provider.isConnectionAlive()) {
                logger.debug("openConnection()");
                //need to open threaded long live session in order to work properly
                provider.openSession(TransportSessionMode.LONG_LIFE_THREADED_MODE);
            } else {
                logger.debug("Connection already opened.");
            }

        }
        if(provider.isTransportProviderListenerSupported()){
            provider.registerTransportProviderListener(adapterTransportProviderListener);
        }

        provider.setAutoConsumeSocketInputData(true);
    }

    @Override
    public void closeConnection() throws TransportException {
        transportManager.closeConnection();
    }

    @Override
    public void  sendData(final TransportData request) throws TransportException {
        try {
            lock.lock();
            while (expectedMessageId.get() != WAITING_MESSAGE_ID) {
                inProcess.await();
            }
            expectedMessageId.set(request.getMessageId());
            //will work as long as Transport manager impl uses async send, so it's up
            // to us whether we want to reuse it or bring the logic to this class
            transportManager.sendData(request);
        } catch (InterruptedException e) {
            logger.error("Wait was interrupted!", e);
            expectedMessageId.set(WAITING_MESSAGE_ID);
            Thread.currentThread().interrupt();
            throw new TransportException(e, TransportSeverity.FAILURE);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void readData(final TransportData response) throws TransportException {
        try {
            lock.lock();
            if (getTransportProvider().isTransportProviderListenerSupported()) {
                processDataWithQueue(response);
            } else {
                transportManager.readData(response);
            }
        }finally {
            expectedMessageId.set(WAITING_MESSAGE_ID);
            inProcess.signalAll();
            lock.unlock();
        }
    }

    private void processDataWithQueue(final TransportData response) throws TransportException {
        try {
            long waitTimeForResponse = getTransportProvider().getSocketTimeout(TimeUnit.MILLISECONDS);
            waitTimeForResponse = waitTimeForResponse != 0 ? waitTimeForResponse : 60000;
            boolean done = false;
           while (!done) {
               final String messageInfo = waitForReplyMessage(waitTimeForResponse, TimeUnit.MILLISECONDS);
               String messageInfoNoEndTag = getMessageInfoNoEndTag(messageInfo);
               if (messageInfo.contains("<hello")) {
                   response.setData(messageInfoNoEndTag.getBytes());
                   done = true;
               } else if (messageInfo.contains("<rpc")) {
                   done = handleRPCResponse(response, messageInfoNoEndTag);
               } else {
                   final boolean isConnectionAlive = this.getTransportProvider().isConnectionAlive();
                   logger.warn("Received unknown message {}. Connection is {} alive", messageInfo, isConnectionAlive ? "still" : "not ");
                   throw new TransportException(messageInfo, TransportSeverity.FATAL);
               }
           }

        } catch (final InterruptedException e) {
            logger.error("Unable to get read result. Thread was interrupted", e);
            Thread.currentThread().interrupt();
            throw new TransportException(e, TransportSeverity.FAILURE);
        } catch (final XPathExpressionException e) {
            logger.error("Unable to parse result!", e);
            Thread.currentThread().interrupt();
            throw new TransportException(e, TransportSeverity.FAILURE);
        }

    }

    private boolean handleRPCResponse(final TransportData response, final String messageInfoNoEndTag) throws XPathExpressionException {
        long messageId = getMessageId(messageInfoNoEndTag);
        if(messageId == -1){
            //if no id found assume that it is rpc error,
            // as this is the only case when rpc reply can have no id, so enriching it with expected id
            messageId = expectedMessageId.get();
        }
        if (messageId != expectedMessageId.get()) {
            logger.error("Received NetconfReply with unexpected messageId: {}. Expected: {}", messageId,
                    expectedMessageId.get());
            return false;
        }
        response.setMessageId(messageId);
        response.setData(messageInfoNoEndTag.getBytes());
        return true;
    }

    private String getMessageInfoNoEndTag(final String messageInfo) {
        if(messageInfo.endsWith("\n##\n")){
            return messageInfo.replaceAll("\n##\n", "");
        }else{
            return messageInfo.replaceAll("]]>]]>", "");
        }
    }

    @Override
    public Queue<String> getNotificationQueue() {
        return notificationQueue;
    }

    @Override
    public void readData(TransportData response, char[] filterCharacters) throws TransportException {
        readData(response);
    }

    private long getMessageId(String messageInfo) throws XPathExpressionException {
        //probably should be replaced with xml parser instead string one
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource is = new InputSource(new StringReader(messageInfo));
        String messageId = xpath.evaluate("/*[local-name()=\"rpc\"]/@*[local-name()=\"message-id\"]", is);
        if(!StringUtils.isEmpty(messageId)) {
            return (long) Double.parseDouble(messageId);
        }else {
            return -1;
        }
    }

    private String waitForReplyMessage(final long waitTimeForResponse, final TimeUnit timeUnit)
            throws InterruptedException, TransportException {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(waitTimeForResponse);
        String messageInfo = null;
        if(getTransportProvider().available() > 0){
            //hello message is sent simultaneously, so sometimes server hello is sent before the listener is enabled,
            // this is workaround for such types of situation
            logger.debug("reading data from input stream!");
            TransportData transportData = new TransportData();
            transportManager.readData(transportData);
            return transportData.getDataAsString();
        }

        logger.debug("reading data from input queue!");
        while (System.currentTimeMillis() < endTime) {
            messageInfo = netconfReplyQueue.poll();
            if(messageInfo != null){
                break;
            }
            Thread.sleep(10);
        }
        if (messageInfo == null) {
            logger.error("Unable to get read result. Read procedure took too much time. More than {} ms", waitTimeForResponse);
            throw new TransportException(new TimeoutException(), TransportSeverity.FATAL);
        }
        return messageInfo;
    }

    @Override
    public InputStream getTransportInputStream() {
        return transportManager.getTransportInputStream();
    }
}
