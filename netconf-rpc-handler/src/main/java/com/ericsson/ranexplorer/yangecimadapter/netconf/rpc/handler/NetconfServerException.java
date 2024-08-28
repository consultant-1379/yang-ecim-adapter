/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler;

public class NetconfServerException extends RuntimeException {
    public NetconfServerException(String message) {
        super(message);
    }

    public NetconfServerException(String message, Throwable e) {
        super(message, e);
    }

    public NetconfServerException(Throwable e) {
        super(e);
    }

    public NetconfServerException() {}
}
