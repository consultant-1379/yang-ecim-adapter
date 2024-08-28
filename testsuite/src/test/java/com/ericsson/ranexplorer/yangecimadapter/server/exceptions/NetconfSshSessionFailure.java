/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.exceptions;

public class NetconfSshSessionFailure extends RuntimeException {

    private static final long serialVersionUID = 1211513421140522218L;

    public NetconfSshSessionFailure(final String message) {
        super(message);
    }

    public NetconfSshSessionFailure(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
