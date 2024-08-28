/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "class")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAddToContext
public class Clazz extends Action {

    private static final Logger logger = LoggerFactory.getLogger(Clazz.class);

    @XmlAttribute
    private String listener;

    public Clazz() {
        //default constructor for JAXB parser
    }

    public Clazz(String listener) {
        this.listener = listener;
    }

    @Override
    public CommandListener apply(final Object env) {
        try {
            return (CommandListener)Class.forName(listener).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error("failed to create new instance of {0} listener", listener, e);
            throw new NetconfServerException("Cannot create listener instance! Error occured: " +  e.getMessage());
        }

    }
}
