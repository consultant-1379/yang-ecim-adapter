/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.exceptions;

public class TimedOutException extends RuntimeException {

    private static final long serialVersionUID = 1211513421140522218L;

    public TimedOutException(final String message) {
        super(message);
    }

    public TimedOutException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
