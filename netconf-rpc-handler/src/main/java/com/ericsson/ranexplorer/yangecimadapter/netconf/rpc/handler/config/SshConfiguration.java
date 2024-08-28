/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement(name = "SshConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SshConfiguration extends Configuration {

    @XmlAttribute
    private boolean allowUsersFromListOnly;
    @XmlElement
    private Map<String, String> users;


    @XmlElementWrapper
    @XmlElement(name = "subsystem")
    private List<String> subsystems;

    public SshConfiguration() {
        //default constructor for JAXB parser
    }

    public SshConfiguration(int port, List<NetconfRule> rules, boolean allowUsersFormListOnly, Map<String, String> users) {
        this(0, 0L, port, rules, allowUsersFormListOnly, users);
    }

    public SshConfiguration(long socketTimeout, long waitForClose, int port, List<NetconfRule> rules, boolean allowUsersFormListOnly, Map<String, String> users) {
        super(socketTimeout, waitForClose, port, rules);
        this.subsystems = new ArrayList<>(Arrays.asList("netconf"));
        this.allowUsersFromListOnly = allowUsersFormListOnly;
        this.users = users;
    }

    public boolean isAllowUsersFromListOnly() {
        return allowUsersFromListOnly;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public List<String> getSubsystems() {
        return subsystems;
    }
}
