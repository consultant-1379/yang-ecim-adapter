/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler;

public enum MessageState {
    MESSAGE_REQUEST, MESSAGE_RESPONSE,
    YANG_TO_ECIM, FILTER_PROCESS, RESPONSE_POST_PROCESS, ERROR, EXCEPTION;
}
