/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;
import static  com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.NETSIM;
import static org.junit.Assert.*;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GetConfigIT {

    private static final Logger LOG = LoggerFactory.getLogger(GetConfigIT.class);

    private static TestProperties props = new TestProperties();

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
    public void testGetConfigWithNamespace(){
        final String adapterInput = "netconf/prefixes/getConfig/getConfigNoPrefixes.xml";
        final String nodeInput = "netconf/getConfig/getConfigEnbIdUsingEcimFilter.xml";

        final String nodeResponse = nsu.getNodeSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(nodeInput)));
        final String expectedValue = getValue(nodeResponse, "eNBId");

        final String adapterResponse = nsu.getAdapterSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(adapterInput)));
        final String actualValue = getValue(adapterResponse, "enb-id");

        assertEquals("Failure: Node value does not match with Adapter value [NodeValue=" + expectedValue + ", " +
                "AdapterValue=" + actualValue + "]", expectedValue, actualValue);
    }

    @Test
    public void testGetConfigWithMO(){

        String[] data;
        if (NETSIM.equals(props.valueOf(TestProperty.NODE_TYPE))) {
            data = new String[]{"LTE01dg2ERBS00001-1", "1", "314"};
        } else {
            // real node data for lienb4916 in Linkoping
            data = new String[]{"2", "1", "eUtranCellRelation2"};
        }
        final String adapterInput = "netconf/yangFilters/in/getEutranCellRelationUsingYangFilter.xml";
        final String adapterResponse = nsu.getAdapterSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(adapterInput, data)));

        final String nodeInput = "netconf/getConfigEutranCellRelationUsingEcimFilter.xml";
        final String nodeResponse = nsu.getNodeSession().sendMessage(getXmlWithoutWhitespace(getXmlFromFile(nodeInput, data)));

        final List<String> yangAttributes = Arrays.asList("cell-individual-offset-eutran", "coverage-indicator", "s-cell-candidate");
        final List<String> ecimAttributes = Arrays.asList("cellIndividualOffsetEUtran", "coverageIndicator", "sCellCandidate");
        assertAttributes(adapterResponse, nodeResponse, yangAttributes, ecimAttributes);
    }

    private void assertAttributes(String adapterResponse, String nodeResponse, List<String> yangAttributes, List<String> ecimAttributes){
        for(int i=0; i<yangAttributes.size(); i++){
            String nodeValue = getValue(nodeResponse, ecimAttributes.get(i));
            String adapterValue = getValue(adapterResponse, yangAttributes.get(i));
            assertEquals("Failure: Node value should be same as adapter value [NodeValue="+nodeValue + ", AdapterValue="+adapterValue + "]",
                    nodeValue.toLowerCase(), adapterValue.toLowerCase());
        }
    }
}
