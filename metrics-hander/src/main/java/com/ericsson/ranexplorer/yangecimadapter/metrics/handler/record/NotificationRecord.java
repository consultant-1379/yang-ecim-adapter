/*
 *  ----------------------------------------------------------------------------
 *  *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 *  * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record;

import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.NotificationStateInfo;

public class NotificationRecord extends Record{
    private static final String RECORD_NAME = "NOTIFICATION";
    private static final String EXTENDED_RECORD_NAME = "EXTENDED_NOTIFICATION_INFO";
    private final int sessionId;
    private long startTime;
    private long endTime;
    private NotificationStateInfo extendedNotificationInfo;

    public NotificationRecord(final int sessionId) {
        this.sessionId = sessionId;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    private String duration() {
        return print((int)(endTime - startTime));
    }

    public void setTransformationStartTime(final long startTime){
        this.extendedNotificationInfo = new NotificationStateInfo(startTime);
    }

    public void setTransformationEndTime(final long endTime){
        if(extendedNotificationInfo != null){
            extendedNotificationInfo.setEndTime(endTime);
        }
    }

    public void log(final String southboundSessionId) {
        logInfo(RECORD_NAME, print(sessionId),southboundSessionId, print(startTime/1000.0), print(endTime/1000.0), duration());
    }

    public void logExtendedNotificationRecord(final String southboundSessionId) {
        logDebug(EXTENDED_RECORD_NAME, print(sessionId), southboundSessionId, duration(), print(extendedNotificationInfo.duration()));
    }
}
