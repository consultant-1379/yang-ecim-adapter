/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.exceptions;

public class FileReadException extends RuntimeException {

    private static final long serialVersionUID = -1483959129443750991L;

    public FileReadException(final String message) {
        super(message);
    }

    public FileReadException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
