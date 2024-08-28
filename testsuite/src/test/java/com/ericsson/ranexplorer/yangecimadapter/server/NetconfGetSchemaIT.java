/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;
import static org.junit.Assert.*;

import java.io.IOException;

import com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfGetSchemaIT {

    private static final Logger logger = LoggerFactory.getLogger(NetconfGetIT.class);
    private static final String TEST_INFO = "Executing {} against realnode";
    private static final String RPC_GET_ENVELOPE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
            " <get>" +
            "  <filter type=\"subtree\">\n" +
            "<netconf-state xmlns= \"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">\n" +
            "%s" +
            "</netconf-state>" +
            "  </filter>" +
            " </get>\n" +
            "</rpc>";

    private static final TestProperties PROPERTIES = new TestProperties();
    private static NetconfSessionsUtil nsu;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setUpSessions(){
        try {
            nsu = NetconfSessionsUtil.getInstance();
        }catch (IOException exception){
            logger.error("Exception thrown while creating sessions with error [{}]", exception.getMessage(), exception);
            fail();
        }
    }

    @Before
    public void openSessions(){
        try {
            nsu.openSessions();
        }catch (IOException exception){
            logger.error("Exception thrown while creating sessions in [{}] with error [{}]", testName.getMethodName(),
                    exception.getMessage(), exception);
            fail();
        }
    }

    @After
    public void closeSessions(){
        nsu.closeSessions();
    }

    @Test
    public void testNetconfGetSchemasIT(){
        if(IntTestConstants.REALNODE.equals(PROPERTIES.valueOf(TestProperty.NODE_TYPE))) {
            logger.info(TEST_INFO, testName.getMethodName());
            String filterStr = "<schemas/>\n";
            String command = String.format(RPC_GET_ENVELOPE, filterStr);
            String result = nsu.getAdapterSession().sendMessage(command);
            assertEquals(getStringWithoutWhitespace(getXmlFromFile("netconf/netconf-state.xml")),
                    getStringWithoutWhitespace(result));
        }
    }

    @Test
    public void testNetconfGetCapabilitiesIT(){
        if(IntTestConstants.REALNODE.equals(PROPERTIES.valueOf(TestProperty.NODE_TYPE))) {
            logger.info(TEST_INFO, testName.getMethodName());
            String filterStr = "<capabilities/>\n";
            String command = String.format(RPC_GET_ENVELOPE, filterStr);
            String result = nsu.getAdapterSession().sendMessage(command);
            assertEquals(getStringWithoutWhitespace(getXmlFromFile("netconf/netconf-capabilities.xml")),
                    getStringWithoutWhitespace(result));
        }
    }

    @Test
    public void testNetconfGetDatastoresIT(){
        if(IntTestConstants.REALNODE.equals(PROPERTIES.valueOf(TestProperty.NODE_TYPE))) {
            logger.info(TEST_INFO, testName.getMethodName());
            String filterStr = "<datastores/>\n";
            String command = String.format(RPC_GET_ENVELOPE, filterStr);
            String result = nsu.getAdapterSession().sendMessage(command);
            assertEquals(getStringWithoutWhitespace(getXmlFromFile("netconf/netconf-datastores.xml")),
                    getStringWithoutWhitespace(result));
        }
    }

    @Test
    public void testNetconfGetSchemasAndCapabilitiesIT(){
        if(IntTestConstants.REALNODE.equals(PROPERTIES.valueOf(TestProperty.NODE_TYPE))) {
            logger.info(TEST_INFO, testName.getMethodName());
            String filterStr = "<schemas/>\n" + "<capabilities/>\n";
            String command = String.format(RPC_GET_ENVELOPE, filterStr);
            String result = nsu.getAdapterSession().sendMessage(command);
            assertEquals(getStringWithoutWhitespace(getXmlFromFile("netconf/netconf-state-mixed.xml")),
                    getStringWithoutWhitespace(result));
        }
    }

    @Test
    public void testNetconfGetSchemaIT(){
            String command = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    " <get-schema\n" +
                    "    xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">\n" +
                    "        <identifier>ericsson-adapter-yang-extensions</identifier>\n" +
                    "        <version>2008-06-01</version>\n" +
                    "  </get-schema>" +
                    "</rpc>";
            String result = nsu.getAdapterSession().sendMessage(command);
            assertEquals(getXmlWithoutWhitespace(getStringWithoutWhitespace(getXmlFromFile("netconf/ericsson-adapter-yang-extensions.xml"))),
                    getStringWithoutWhitespace(result));
    }
}
