/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.client;

import com.ericsson.oss.mediation.transport.api.provider.TransportProviderListener;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MetricsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class AdapterTransportProviderListener implements TransportProviderListener {
    private static final Logger logger = LoggerFactory.getLogger(AdapterTransportProviderListener.class);
    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;
    private final Queue<String> netconfReplyQueue;
    private final Queue<String> notificationQueue;
    private StringBuilder message = new StringBuilder();
    private int sessionId;

    AdapterTransportProviderListener(final int sesisonId, Queue<String> netconfReplyQueue,
                                            Queue<String> notificationQueue) {
        this.netconfReplyQueue = netconfReplyQueue;
        this.notificationQueue = notificationQueue;
        this.sessionId = sesisonId;
    }

    @Override
    public void channelClosed() {
        logger.info("channelClosed");
    }

    @Override
    public void channelEOF() {
        logger.info("channelEOF");
    }

    @Override
    public void channelOpened() {
        logger.info("channelOpened");
    }

    @Override
    public void dataReceived(byte[] bytes, int offset, int len) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(len);
        try {
            stream.write(bytes, offset, len);
            String tmpMsg = stream.toString(StandardCharsets.UTF_8.name()).trim();
            message.append(tmpMsg);

            if(tmpMsg.endsWith("]]>]]>") || tmpMsg.endsWith("\n##\n")) {
                String finalMessage = message.toString();
                for (String individualNotificationMsg : finalMessage.split("]]>]]>")) {
                    if (finalMessage.contains("<notification")) {
                        if(!finalMessage.contains("*")){
                            logger.info("Data received: {} and added to notificationQueue",
                                    notificationQueue.offer(individualNotificationMsg));
                            METRICS_HANDLER.notificationReceived(sessionId);
                        }
                    } else {
                        logger.info("Data received: {} and added to netconfReplyQueue", netconfReplyQueue.offer(finalMessage));
                    }
                }
                message = new StringBuilder();
            }
        } catch (IOException e) {
            logger.error("Error occurred on dataReceive!", e);
        }
    }

    @Override
    public void dataSent(byte[] bytes, int offset, int len) {
        logger.info("dataSent");
    }
}
