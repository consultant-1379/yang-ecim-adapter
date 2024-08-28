/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;

public abstract class Record {

    private static final Logger logger = LoggerFactory.getLogger("metrics");
    private static NumberFormat formatter = NumberFormat.getInstance();
    protected static final Character SEPERATOR = ',';

    protected Record() {
        formatter.setGroupingUsed(false);
    }

    protected void logInfo(final String... values) {
        if(logger.isInfoEnabled()){
            String record = buildRecord(values);
            logger.info(record);
        }
    }

    protected void logDebug(final String... values) {
        if(logger.isDebugEnabled()){
            String record = buildRecord(values);
            logger.debug(record);
        }
    }

    private String buildRecord(final String... values){
        final StringBuilder record = new StringBuilder();
        for (String value : values) {
            record.append(value).append(SEPERATOR);
        }
        record.deleteCharAt(record.length() - 1);
        return record.toString();
    }

    protected String print(final int value) {
        return Integer.toString(value);
    }

    protected static String print(final double value) {
        formatter.setGroupingUsed(false);
        formatter.setMinimumFractionDigits(3);
        return formatter.format(value);
    }
}
