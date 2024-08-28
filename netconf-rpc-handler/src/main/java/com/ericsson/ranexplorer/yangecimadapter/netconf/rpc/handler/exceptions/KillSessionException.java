package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions;
/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
public class KillSessionException extends RuntimeException{
    public KillSessionException(String message) {
        super(message);
    }

    public KillSessionException(String message, Throwable e) {
        super(message, e);
    }

    public KillSessionException(Throwable e) {
        super(e);
    }
}
