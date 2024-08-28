/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MetricsHandler {

    INSTANCE;

    private Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    public void markStartCreateSession(final int sessionId) {
        Session session = new Session(sessionId, currentTime());
        sessions.put(sessionId, session);
    }

    public void setSouthboundSession(final int sessionId, final String southboundSessionId){
        Session session = sessions.get(sessionId);
        session.setSouthboundSessionId(southboundSessionId);
    }

    public void markEndCreateSession(final int sessionId) {
        Session session = sessions.get(sessionId);
        session.sessionCreated(currentTime());
    }

    public void setMessageId(final int sessionId, final String messageId) {
        Session session = sessions.get(sessionId);
        session.setMessageId(messageId);
    }

    public void setRpcOperation(final int sessionId, final RpcOperation rpcOperation) {
        Session session = sessions.get(sessionId);
        session.setRpcOperation(rpcOperation);
    }

    public void markStart(final int sessionId, final MessageState messageState) {
        Session session = sessions.get(sessionId);
        session.setMessageStateStarted(messageState, currentTime());
    }

    public void markEnd(final int sessionId, final MessageState messageState) {
        Session session = sessions.get(sessionId);
        session.setMessageStateEnded(messageState, currentTime());
    }

    public void markStart(final int sessionId, final NotificationState notificationState) {
        Session session = sessions.get(sessionId);
        session.setNotificationStateStarted(notificationState, currentTime());
    }

    public void markEnd(final int sessionId, final NotificationState notificationState) {
        Session session = sessions.get(sessionId);
        session.setNotificationStateEnded(notificationState, currentTime());
    }

    public void markReset(final int sessionId) {
        Session session = sessions.get(sessionId);
        session.deleteMessageRecord();
    }

    public void notificationReceived(final int sessionId){
        Session session = sessions.get(sessionId);
        session.notificationReceived(currentTime());
    }

    public void notificationSent(final int sessionId){
        Session session = sessions.get(sessionId);
        session.notificationSent(currentTime());
    }

    public void dropNotificationRecord(final int sessionId){
        Session session = sessions.get(sessionId);
        session.dropNotificationRecordStartTime();
    }

    public void removeSession(final int sessionId){
        sessions.remove(sessionId);
    }
    private long currentTime() {
        return System.currentTimeMillis();
    }

}
