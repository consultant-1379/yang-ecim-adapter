/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.common.services.util;

public class Timer {
    long start;
    public Timer(){
        this.start = System.currentTimeMillis();
    }

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public long getElapsedTime(){
        return System.currentTimeMillis() - start;
    }

    public long now(){
        return System.currentTimeMillis();
    }

    public long getDuration(long from){
        return System.currentTimeMillis() - from;
    }
}
