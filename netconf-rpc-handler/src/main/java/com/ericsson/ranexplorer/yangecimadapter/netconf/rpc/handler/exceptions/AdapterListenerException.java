/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions;

public class AdapterListenerException extends RuntimeException {
    public AdapterListenerException(String message) {
        super(message);
    }

    public AdapterListenerException(String message, Throwable e) {
        super(message, e);
    }

    public AdapterListenerException(Throwable e) {
        super(e);
    }
}
