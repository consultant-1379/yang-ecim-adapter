/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config;

import com.ericsson.ranexplorer.yangecimadapter.common.services.netconf.capabilities.CapabilityService;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class CapabilityServiceTest {
    private static final String CAPABILITY_TO_NODE = "capabilities.to.node";

    @Test
    public void testGetToCapabilitiesAsList(){
        List<String> netconfCapabilitiesCheck = Arrays.asList(//common capabilities
                "urn:ietf:params:netconf:base:1.0", "urn:ietf:params:netconf:capability:notification:1.0",
                "urn:ietf:params:netconf:capability:candidate:1.0", "urn:ietf:params:netconf:capability:validate:1.0",
                "urn:ericsson:com:netconf:heartbeat:1.0","urn:com:ericsson:ebase:1.1.0","urn:com:ericsson:ebase:1.2.0");

        List<String> netconfCapabilities = new CapabilityService().getCapabilitiesAsList(CAPABILITY_TO_NODE);
        assertEquals(netconfCapabilities, netconfCapabilitiesCheck);
    }


    @Test
    public void testGetToCapabilitiesAsXML(){
        String checkString = "\t\t<capability>urn:ietf:params:netconf:base:1.0</capability>" +
                "\t\t<capability>urn:ietf:params:netconf:capability:notification:1.0</capability>" +
                "\t\t<capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>" +
                "\t\t<capability>urn:ietf:params:netconf:capability:validate:1.0</capability>" +
                "\t\t<capability>urn:ericsson:com:netconf:heartbeat:1.0</capability>" +
                "\t\t<capability>urn:com:ericsson:ebase:1.1.0</capability>" +
                "\t\t<capability>urn:com:ericsson:ebase:1.2.0</capability>";

        String capabilitiesXml = new CapabilityService().getCapabilitiesAsXML(CAPABILITY_TO_NODE);
        assertEquals(checkString, capabilitiesXml);

    }
}
