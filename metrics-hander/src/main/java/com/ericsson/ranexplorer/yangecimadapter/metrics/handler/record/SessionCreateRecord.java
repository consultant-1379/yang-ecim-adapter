/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record;

public class SessionCreateRecord extends Record {

    private static final String RECORD_NAME = "SESSION_CREATE";

    private final int sessionId;
    private String southboundSessionId;
    private final long startTime;
    private long endTime;

    public SessionCreateRecord(final int sessionId, final long startTime) {
        this.sessionId = sessionId;
        this.startTime = startTime;
    }

    public void setSouthboundSessionId(final String southboundSessionId){
        this.southboundSessionId = southboundSessionId;
    }

    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    private String duration() {
        return print((int)(endTime - startTime));
    }

    public void log() {
        logInfo(RECORD_NAME, print(sessionId), southboundSessionId, print(startTime/1000.0), print(endTime/1000.0), duration());
    }

}
