package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

public class NotificationStateInfo {

    private final long startTime;
    private long endTime;

    public NotificationStateInfo(final long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }

    public int duration() {
        return (int) (endTime - startTime);
    }
}