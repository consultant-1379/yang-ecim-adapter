/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.exceptions;

public class MessageFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MessageFailedException(final String message) {
        super(message);
    }

    public MessageFailedException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
