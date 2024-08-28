/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.exceptions;

public class ParsingFailedException extends RuntimeException {

    private static final long serialVersionUID = 1211513421140522218L;

    public ParsingFailedException(final String message) {
        super(message);
    }

    public ParsingFailedException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
