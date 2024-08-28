/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

//Sonar wants interface, but JAXB doesn't support it
public abstract class Action {//NOSONAR
    public abstract CommandListener apply(final Object sttmt);
}
