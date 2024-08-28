/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

public class MessageStateInfo {

    private final long startTime;
    private long endTime;

    public MessageStateInfo(final long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    public int duration() {
        return (int) (endTime - startTime);
    }

    public boolean isEndTimeSet(){
        return endTime > 0;
    }

    public boolean isEndTimeNotSet(){
        return !isEndTimeSet();
    }

    public boolean isStartTimeSet(){
        return startTime > 0;
    }
}
