/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record.MessageRecord;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record.NotificationRecord;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record.SessionCreateRecord;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Session {

    private SessionCreateRecord sessionCreateRecord;
    private MessageRecord messageRecord;
    private Queue<Long> notificationStartTimeQueue;
    private NotificationRecord notificationRecord;
    private int sessionId;
    private String southboundSessionId;

    public Session(final int sessionId, final long startTime) {
        this.sessionId = sessionId;
        sessionCreateRecord = new SessionCreateRecord(sessionId, startTime);
        this.notificationStartTimeQueue= new ConcurrentLinkedQueue<>();
    }

    public void setSouthboundSessionId (final String southboundSessionId){
        this.southboundSessionId = southboundSessionId;
        if(sessionCreateRecord != null){
            sessionCreateRecord.setSouthboundSessionId(southboundSessionId);
        }
    }

    public void sessionCreated(final long endTime) {
        sessionCreateRecord.setEndTime(endTime);
        sessionCreateRecord.log();
        sessionCreateRecord = null;
    }

    public void setMessageId(final String messageId) {
        if (messageRecord != null) {
            messageRecord.setMessageId(messageId);
        }
    }

    public void notificationReceived(final long startTime){
        notificationStartTimeQueue.add(startTime);
    }

    public void setRpcOperation(final RpcOperation rpcOperation) {
        if (messageRecord != null) {
            messageRecord.setRpcOperation(rpcOperation);
        }
    }

    public void setMessageStateStarted(final MessageState messageState, final long startTime) {
        createMessageRecordIfStartState(messageState);
        if (messageRecord != null) {
            messageRecord.setStartTime(messageState, startTime);
        }
    }

    private void createMessageRecordIfStartState(final MessageState messageState) {
        if (MessageState.MESSAGE_REQUEST.equals(messageState)) {
            messageRecord = new MessageRecord(sessionId);
        }
    }

    public void setMessageStateEnded(final MessageState messageState, final long endTime) {
        if (messageRecord != null) {
            messageRecord.setEndTime(messageState, endTime);
            logMessageRecord(messageState);
        }
    }

    public void setNotificationStateStarted (final NotificationState notificationState, final long startTime){
        if(NotificationState.TRANSLATION.equals(notificationState)){
            notificationRecord = new NotificationRecord(sessionId);
            notificationRecord.setTransformationStartTime(startTime);
        }
    }

    public void setNotificationStateEnded (final NotificationState notificationState, final long endTime){
        if(NotificationState.TRANSLATION.equals(notificationState)){
            notificationRecord.setTransformationEndTime(endTime);
        }
    }

    public void notificationSent(final long endTime) {
        if (notificationRecord != null) {
            notificationRecord.setStartTime(notificationStartTimeQueue.remove());
            notificationRecord.setEndTime(endTime);
            notificationRecord.log(southboundSessionId);
            notificationRecord.logExtendedNotificationRecord(southboundSessionId);
            deleteNotificationRecord();
        }
    }

    private void logMessageRecord(final MessageState messageState) {
        if (MessageState.MESSAGE_RESPONSE.equals(messageState)) {
            messageRecord.log(southboundSessionId);
            messageRecord.logExtendedMessageRecord(southboundSessionId);
            messageRecord = null;
        }
    }

    public void deleteMessageRecord() {
        messageRecord = null;
    }

    public void deleteNotificationRecord(){
        notificationRecord = null;
    }

    public void dropNotificationRecordStartTime(){
        notificationStartTimeQueue.remove();
        if(notificationRecord != null){
            deleteNotificationRecord();
        }
    }
}
