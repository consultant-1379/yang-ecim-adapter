/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions;

public class FilterProcessingException extends RuntimeException {

    private static final long serialVersionUID = -123565452444379991L;

    public FilterProcessingException(String message){
        super(message);
    }

    public FilterProcessingException(String message, Throwable cause){
        super(message, cause);
    }
}
