/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.util;

import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.MessageFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.StringUtils.removeInitialAndTerminationStringFrom;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getXmlWithoutWhitespace;

public class NetconfNotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NetconfNotificationHandler.class);

    private static final long REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private static final long WAIT_PERIOD = 500;

    private NetconfSession netconfSession;

    private boolean awaitingNotifications;
    private List<String> notificationList = new CopyOnWriteArrayList<>();

    public NetconfNotificationHandler(NetconfSession netconfSession) {
        this.netconfSession = netconfSession;
        awaitingNotifications = true;
        new Thread(this::awaitNotifications).start();
    }

    private void awaitNotifications() {
        logger.debug("Waiting for notifications.....");
        while (awaitingNotifications && netconfSession.channelIsOpen()) {
            if (isOutputStreamTerminatedAndContainsNotification()){
                String rpcReplys = removeInitialAndTerminationStringFrom(getRpcReplyAndResetOutputStream());
                logger.debug("Notifications received together:\n{}", rpcReplys);

                for (String rpcReply : rpcReplys.split(ONE_DOT_ZERO_TERMINATION)) {
                    if (isNotification(rpcReply)) {
                        logger.debug("RECEIVED NOTIFICATION:\n{}", rpcReply);
                        notificationList.add(getXmlWithoutWhitespace(rpcReply));
                    }
                }
            } else {
                try {
                    Thread.sleep(WAIT_PERIOD);
                } catch (final InterruptedException exception) {
                    logger.error("Exception thrown on netconf session {} while trying to receive notification",
                            netconfSession.getName(), exception);
                    Thread.currentThread().interrupt();
                    throw new MessageFailedException(String.format("Failed to receive notification from %s",
                            netconfSession.getName()), exception);
                }
            }
        }
        logger.debug("Notification handling stopped");
    }

    public boolean isNotificationReceived(final String patternString) throws InterruptedException {
        logger.debug("Checking for notification... (timeout {} ms)", REQUEST_TIMEOUT);
        final long startTime = System.currentTimeMillis();
        boolean notificationFound = false;
        Pattern pattern = Pattern.compile(patternString);
        while (!notificationFound) {
            for (String notification : notificationList) {
                Matcher matcher = pattern.matcher(notification);
                if (matcher.matches()) {
                    notificationFound = true;
                    break;
                }
            }
            if (System.currentTimeMillis() > (startTime + REQUEST_TIMEOUT)) {
                logger.warn("TimeOut: No notification received matching pattern: {}", patternString);
                for (String notification : notificationList) {
                    logger.warn(notification);
                }
                break;
            }
            if (!notificationFound)
                Thread.sleep(WAIT_PERIOD);
        }
        return notificationFound;
    }

    public void end() {
        awaitingNotifications = false;
    }

    private Boolean isOutputStreamTerminatedAndContainsNotification() {
        // using trim() because extra line breaks are added to end of responses
        return netconfSession.getOutputStream().toString().trim().endsWith(ONE_DOT_ONE_TERMINATION.trim())
                || netconfSession.getOutputStream().toString().trim().endsWith(ONE_DOT_ZERO_TERMINATION)
                && netconfSession.getOutputStream().toString().contains("<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">");
    }

    private Boolean isNotification(String netconfXml) {
        return netconfXml.contains("<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">");
    }

    private String getRpcReplyAndResetOutputStream() {
        String response = netconfSession.getOutputStream().toString();
        netconfSession.getOutputStream().reset();
        return response;
    }
}
