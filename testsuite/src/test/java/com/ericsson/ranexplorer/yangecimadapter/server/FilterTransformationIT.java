/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getXmlWithoutWhitespace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper;

import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterTransformationIT {
    
    @Rule
    public final TestName testName = new TestName();

    private static final Logger LOG = LoggerFactory.getLogger(FilterTransformationIT.class);
    private static final String QCI_PROFILE_PREDEFINED_ID = "9";

    private static TestProperties props = new TestProperties();
    private static NetconfSessionsUtil nsu;

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
    public void testFilterTransformation_usingSingleAttribute(){ //NOSONAR

        final String qciQuantaNetsim = "netconf/getQciSubscriptionQuantaFromNode.xml";
        final String qciQuantaAdapter = "netconf/yangFilters/in/getSingleAttributeUsingYangFilter.xml";
        final String assertionFailureMsg = "Failure - Quanta value received using ECIM filter is not the same as YANG filter";

        String qciId = QCI_PROFILE_PREDEFINED_ID;
        if (REALNODE.equals(props.valueOf(TestProperty.NODE_TYPE))) {
            qciId = "qci" + QCI_PROFILE_PREDEFINED_ID;
        }
        try {
            final String netsimResponse = nsu.getNodeSession().sendMessage(getXmlFromFile(qciQuantaNetsim, qciId));
            final int ecimFilterData = Integer.parseInt(XmlHelper.getValue(netsimResponse, "qciSubscriptionQuanta"));

            final String adapterResponse = nsu.getAdapterSession().sendMessage(String.format(RPC_WRAPPER, "2", getXmlFromFile(qciQuantaAdapter, qciId)));
            final int yangFilterData = Integer.parseInt(XmlHelper.getValue(adapterResponse, "qci-subscription-quanta"));

            assertEquals(assertionFailureMsg, ecimFilterData, yangFilterData);

        }catch(Exception exception){
            LOG.error("Exception thrown while executing {} : {}", testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testFilterTransformation_usingMO_noMasking(){ //NOSONAR

        final String cellRelationMoAdapter = "netconf/yangFilters/in/getMOUsingYangFilter.xml";

        String[] inData;
        if (NETSIM.equals(props.valueOf(TestProperty.NODE_TYPE))) {
            inData = new String[]{"LTE01dg2ERBS00001-1", "1", "314"};
        } else {
            // real node data for lienb4916 in Linkoping
            inData = new String[]{"2", "1", "eUtranCellRelation2"};
        }

        try{
            final String response = nsu.getAdapterSession().sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(cellRelationMoAdapter, inData)));

            Pattern pattern = Pattern.compile(".*" +
                    "<enodeb-function xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">" +
                    "<eutran-cell-fdd>" +
                    "<id>.*</id>" +
                    "<eutran-freq-relation>" +
                    "<id>\\d+</id>" +
                    "<eutran-cell-relation>" +
                    "<id>.*</id>" +
                    "<cell-individual-offset-eutran>\\d+</cell-individual-offset-eutran>" +
                    "<neighbor-cell-ref xmlns:lrtadpt=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">.*</neighbor-cell-ref>" +
                    "<q-offset-cell-eutran>\\d+</q-offset-cell-eutran>" +
                    "<coverage-indicator>.*</coverage-indicator>" +
                    "<load-balancing>.*</load-balancing>" +
                    "<s-cell-candidate>.*</s-cell-candidate>" +
                    "<s-cell-priority>\\d+</s-cell-priority>" +
                    "</eutran-cell-relation>" +
                    "</eutran-freq-relation>" +
                    "</eutran-cell-fdd>" +
                    "</enodeb-function>" +
                    ".*");
            Matcher matcher = pattern.matcher(getXmlWithoutWhitespace(response));
            assertTrue(matcher.matches());
        }catch(Exception exception){
            LOG.error("Exception thrown while executing {}: {}", testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testFilterTransformation_emptyFilter_noDataReturned(){ //NOSONAR
        final String emptyFilter = "netconf/yangFilters/in/getWithAnEmptyFilter.xml";
        final String expectedRpc = "netconf/yangFilters/out/getWithEmptyFilterRpcReply.xml";

        if (props.isRealNodeTest()) {
            try {
                final String response = nsu.getAdapterSession().sendMessage(getXmlFromFile(emptyFilter));
                assertEquals(getXmlWithoutWhitespace(getXmlFromFile(expectedRpc)), getXmlWithoutWhitespace(response));
            } catch (Exception exception) {
                LOG.error("Exception thrown while executing {}: {}", testName.getMethodName(), exception.getMessage(), exception);
                fail();
            }
        }
    }
}
