/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import com.ericsson.oss.mediation.util.netconf.api.error.*;
import com.ericsson.oss.mediation.util.netconf.api.error.Error;

public class NetconfErrorHandler {

    private NetconfErrorHandler() {
    }

    public static Error createOperationFailedError(final String errorMessage) {
        return createError(errorMessage, ErrorTag.OPERATION_FAILED);
    }

    public static Error createError(final String errorMessage, final ErrorTag errorTag) {
        return createError(errorMessage, errorTag, ErrorType.application);
    }

    public static Error createError(final String errorMessage, final ErrorTag errorTag, final ErrorType errorType) {
        Error error = new Error();
        error.setErrortype(errorType);
        error.setErrorTag(errorTag);
        error.setErrorSeverity(ErrorSeverity.error);
        error.setErrorMessage(errorMessage);
        return error;
    }

    public static Error createUnsupportedDataStoreError(final String dataSource) {
        String msg = String.format("%s datastore is not supported", dataSource);
        return createError(msg, ErrorTag.OPERATION_NOT_SUPPORTED, ErrorType.protocol);
    }

    public static Error createXpathFilterNotSupportedError() {
        String msg = "xpath filtering is not supported";
        return createError(msg, ErrorTag.OPERATION_NOT_SUPPORTED, ErrorType.protocol);
    }

}
