/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.common.services.netconf.capabilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.*;

public class CapabilityService {

    private final PropertiesConfiguration config;
    private static final List<String> DEFAULT_CAPABILITIES = Collections.singletonList("urn:ietf:params:netconf:base:1.0");

    public CapabilityService(){
        String capabilityFileLocation = System.getProperty("CAPABILITY_LOCATION");
        if(capabilityFileLocation == null){
            capabilityFileLocation = "capabilities.properties";
        }

        try {
            config = new PropertiesConfiguration(capabilityFileLocation);
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Failed to Initialize configuration", e);
        }
    }


    public List<String> getCapabilitiesAsList(String propertyName){
        String toCapabilitiesStr = config.getString(propertyName);
        if(toCapabilitiesStr == null){
            return DEFAULT_CAPABILITIES;
        }
        return Arrays.asList(toCapabilitiesStr.split(","));
    }

    public String getCapabilitiesAsXML(String propertyName){
        String toCapabilitiesStr = config.getString(propertyName);
        List<String> capabilities;
        if(toCapabilitiesStr == null){
            capabilities = DEFAULT_CAPABILITIES;
        }
        else {
            capabilities = Arrays.asList(toCapabilitiesStr.split(","));
        }
        StringBuilder stringBuffer = new StringBuilder();
        for(String capability : capabilities){
            stringBuffer.append(String.format("\t\t<capability>%s</capability>", capability));
        }
        return stringBuffer.toString();
    }

}
