/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import org.junit.*;

import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GetConfigPrefixIT {

    private static final Logger LOG = LoggerFactory.getLogger(GetConfigPrefixIT.class);
    private static NetconfSessionsUtil nsu;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setUpSessions(){
        try {
            nsu = NetconfSessionsUtil.getInstance();
        }catch (IOException exception){
            LOG.error("Exception thrown while creating sessions with error [{}]", exception.getMessage(), exception);
            fail();
        }
    }

    @Before
    public void openSessions(){
        try {
            nsu.openSessions();
        }catch (IOException exception){
            LOG.error("Exception thrown while creating sessions in [{}] with error [{}]", testName.getMethodName(),
                    exception.getMessage(), exception);
            fail();
        }
    }

    @After
    public void closeSessions(){
        nsu.closeSessions();
    }

    @Test
    public void testGetConfigWithNoPrefix(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigNoPrefixes.xml";
        final String expectedValue = getExpectedValueFromNode();
        final String actualValue = getActualValueFromAdapter(adapterInput);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testGetConfigWithPrefixOnAll(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigPrefixesOnAll.xml";
        final String expectedValue = getExpectedValueFromNode();
        final String actualValue = getActualValueFromAdapter(adapterInput);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testGetConfigWithPrefixOnInnerMO(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigWithInnerMoPrefixOnly.xml";
        final String expectedValue = getExpectedValueFromNode();
        final String actualValue = getActualValueFromAdapter(adapterInput);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testGetConfigWithRpcPrefixOnly(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigWithRpcPrefixOnly.xml";
        final String expectedValue = getExpectedValueFromNode();
        final String actualValue = getActualValueFromAdapter(adapterInput);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testGetConfigWithPrefixAndNamespaceInRootTag(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigPrefixesOnAllAndNamespacesInRoot.xml";
        final String expectedValue = getExpectedValueFromNode();
        final String actualValue = getActualValueFromAdapter(adapterInput);
        assertEquals(expectedValue, actualValue);
    }

    private String getActualValueFromAdapter(final String input){
        final String adapterResponse = nsu.getAdapterSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(input)));
        return getValue(adapterResponse, "enb-id");
    }

    private String getExpectedValueFromNode(){
        final String nodeInput = "netconf/getConfig/getConfigEnbIdUsingEcimFilter.xml";
        final String nodeResponse = nsu.getNodeSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(nodeInput)));
        return getValue(nodeResponse, "eNBId");
    }
}
