/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.DefaultCommandListener;
import javax.xml.bind.annotation.*;
import java.util.*;

@XmlTransient
@XmlSeeAlso({TlsConfiguration.class, SshConfiguration.class})
public abstract class Configuration {

    @XmlElementWrapper
    @XmlElement(name = "rule")
    private List<NetconfRule> rules;
    @XmlAttribute
    private int port;
    @XmlAttribute
    private long socketTimeout;

    @XmlAttribute
    private String nodeAddress;

    /**
     * Address which is used by Adapter to listen to messages
     */
    @XmlAttribute
    private String adapterBindAddress;

    /**
     * Address which is used by Adapter to start NetconfManager
     */
    @XmlAttribute
    private String nodeBindAddress;

    @XmlAttribute
    private int nodePort;


    @XmlAttribute
    private long waitForClose;

    public Configuration() {
        //default constructor for JAXB parser
    }

    public Configuration(int port, final List<NetconfRule> rules) {
        this(0, 0, port, rules);
    }

    public Configuration(long socketTimeout, long waitForClose, int port, final List<NetconfRule> rules) {
        this.rules = Collections.unmodifiableList(rules);
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.waitForClose = waitForClose;
    }

    public CommandListener getListener(final Object env) {
        if (rules != null) {
            for (final NetconfRule rule : rules) {
                if (rule.when(env)) {
                    return rule.then(env);
                }
            }
        }
        return new DefaultCommandListener();
    }

    public int getPort() {
        return port;
    }

    public long getSocketTimeout() {
        return socketTimeout;
    }

    public long getWaitForClose() {
        return waitForClose;
    }

    public String getNodeAddress(){
        return nodeAddress;
    }

    public void setNodeAddress(String address){
        nodeAddress = address;
    }

    public int getNodePort() { return nodePort; }

    public void setNodePort(int nodePort) { this.nodePort = nodePort; }

    public String getAdapterBindAddress() {
        if(adapterBindAddress == null){
            //setting up default ip to listen to all networks
            adapterBindAddress = "0.0.0.0";//NOSONAR
        }
        return adapterBindAddress;
    }

    public void setAdapterBindAddress(String adapterBindAddress) {
        this.adapterBindAddress = adapterBindAddress;
    }

    public String getNodeBindAddress() {
        if(this.nodeBindAddress == null){
            //setting up default ip to listen to all networks
            this.nodeBindAddress = "0.0.0.0";//NOSONAR
        }
        return nodeBindAddress;
    }

    public void setNodeBindAddress(String nodeBindAddress) {
        this.nodeBindAddress = nodeBindAddress;
    }
}
