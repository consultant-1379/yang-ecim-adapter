/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ADAPTER;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfVersion.NETCONF_1_1;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getValue;
import static org.junit.Assert.*;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSession;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfVersion;
import com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditConfigIT {

    private static final Logger logger = LoggerFactory.getLogger(EditConfigIT.class);

    private static final String GET_QCI_SUB_QUANTA_NETSIM = "netconf/getQciSubscriptionQuantaFromNode.xml";
    private static final String EDIT_QCI_SUB_QUANTA = "netconf/editQciSubscriptionQuantaYang.xml";
    private static final String QCI_SUB_QUANTA = "qciSubscriptionQuanta";
    private static final String GET_ATTRIBUTES_FILE =
            "netconf/editMultipleOperations/getMultipleAttributesFromNetsim.xml";
    private static final String GET_MOS_FILE =
            "netconf/editMultipleOperations/getMultipleMOsFromNetsim.xml";
    private static final String CREATE_OBJECTS_FILE =
            "netconf/editMultipleOperations/createObjects.xml";
    private static final String EDIT_MULTIPLE_OPERATIONS_FILE =
            "netconf/editMultipleOperations/editMultipleOperations.xml";
    private static final String RESET_EDIT_MULTIPLE_FILE =
            "netconf/editMultipleOperations/resetEditMultiple.xml";
    private static final String LB_THRESHOLD = "lbThreshold";
    private static final String LB_CEILING = "lbCeiling";
    private static final String LOGICAL_CHANNEL_GROUP_REF = "logicalChannelGroupRef";
    private static final String QCI_PROFILE_PREDEFINED_ID = "9";
    private static final String OK_XML_TAG = "<ok/>";
    private static final String ONE_DOT_ZERO_TERMINATION = "]]>]]>";
    private static final String ONE_DOT_ONE_TERMINATION = "\n##\n";
    private static final String NEIGHBOR_CELL_REF_VALUE= "ManagedElement=LTE01dg2ERBS00001,ENodeBFunction=1," +
            "EUtraNetwork=1,ExternalENodeBFunction=LTE02ERBS00029,ExternalEUtranCellFDD=LTE02ERBS00029-4";
    private static final String QCI_PROFILE_REF_VALUE = "ManagedElement=LTE01dg2ERBS00001,ENodeBFunction=1," +
            "QciTable=default,QciProfilePredefined=1";
    private static final String LOGICAL_CHANNEL_GROUP_REF_VALUE = "ManagedElement=LTE01dg2ERBS00001,ENodeBFunction=1,QciTable=default,LogicalChannelGroup=1";
    private static final Pattern messageStartPattern = Pattern.compile("#[0-9]+\n");

    private static TestProperties props = new TestProperties();
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
    public void testEditConfig_updateSingleAttribute_attributeUpdated() { //NOSONAR
            updateSingleAttributeWithAssertions(nsu.getAdapterSession(), nsu.getNodeSession());
    }

    private void updateSingleAttributeWithAssertions(NetconfSession adapterNetconfSession,
                                                     NetconfSession netsimNetconfSession) {

        String response;

        //Get current value of qciSubscriptionQuanta from Netsim or via adapter
        response = netsimNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        final int origQciSubscriptionQuanta = Integer.parseInt(getValue(response, QCI_SUB_QUANTA));

        // Set the new value for qciSubscriptionQuanta using the adapter
        final int newSubscriptionQuantaValue = origQciSubscriptionQuanta + 1;
        response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, Integer.toString(newSubscriptionQuantaValue)));
        assertTrue(response.contains(OK_XML_TAG));
        assertTrue( initialAndTerminationStringsArePresent( response, adapterNetconfSession
                .getVersion() ) );

        // Confirm new value is set in Netsim
        response = netsimNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        int result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        assertEquals(newSubscriptionQuantaValue, result);

        // Reset value back to original
        response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, Integer.toString(origQciSubscriptionQuanta)));
        assertTrue(response.contains(OK_XML_TAG));
        assertTrue( initialAndTerminationStringsArePresent( response, adapterNetconfSession
                .getVersion() ) );

        // Check value is reset
        response = netsimNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        assertEquals(origQciSubscriptionQuanta, result);
    }

    @Test
    public void testNetconf11EditConfig_updateSingleAttribute_attributeUpdated() { //NOSONAR
        NetconfSession adapterNetconfSession = null;
        try {
            adapterNetconfSession = new NetconfSession.Builder().name(ADAPTER).host(props.valueOf(ADAPTER_HOST))
                    .port(props.valueOfInt(ADAPTER_PORT)).user(props.valueOf(SSH_USER)).password(props.valueOf(SSH_PASS))
                    .version(NETCONF_1_1).build();
            adapterNetconfSession.open();

            updateSingleAttributeWithAssertions(adapterNetconfSession, nsu.getNodeSession());
        } catch (final Exception exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        } finally {
            if (adapterNetconfSession != null) {
                adapterNetconfSession.closeAll();
            }
        }
    }

    @Test
    public void testEditConfig_useAllSubOperations_allOperationsSucceeded() { //NOSONAR
            String response;

            //Get current values from Netsim
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_FILE));
            final int origLbThreshold = Integer.parseInt(getValue(response, LB_THRESHOLD));
            final int origLbCeiling = Integer.parseInt(getValue(response, LB_CEILING));
            final String oriLogicalChannelGroupRef = getValue(response, LOGICAL_CHANNEL_GROUP_REF);

            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_OBJECTS_FILE));
            assertTrue(response.contains(OK_XML_TAG));

            // Send edit-config with multiple operations to the adapter
            final int newLbThresholdValue = origLbThreshold + 1;
            final int newLbCeilingValue = origLbCeiling + 1;
            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(
                    EDIT_MULTIPLE_OPERATIONS_FILE,
                    Integer.toString(newLbThresholdValue),
                    Integer.toString(newLbCeilingValue)));
            assertTrue(response.contains(OK_XML_TAG));

            // Confirm new values are set in Netsim
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_MOS_FILE));
            int thresholdInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_THRESHOLD));
            int ceilingInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_CEILING));
            String neighborCellRefResponse = XmlHelper.getValue(response, "neighborCellRef");
            String qciProfileRefResponse = XmlHelper.getValue(response, "qciProfileRef");
            String logicalChannelGroupRefResponse = XmlHelper.getValue(response, LOGICAL_CHANNEL_GROUP_REF);
            assertEquals(newLbThresholdValue, thresholdInResponse);
            assertEquals(newLbCeilingValue, ceilingInResponse);
            assertEquals(NEIGHBOR_CELL_REF_VALUE, neighborCellRefResponse);
            assertEquals(QCI_PROFILE_REF_VALUE, qciProfileRefResponse);
            assertEquals(LOGICAL_CHANNEL_GROUP_REF_VALUE, logicalChannelGroupRefResponse );

            // Reset value back to original
            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(
                    RESET_EDIT_MULTIPLE_FILE,
                    Integer.toString(origLbThreshold),
                    Integer.toString(origLbCeiling)));
            assertTrue(response.contains(OK_XML_TAG));

            // Check values are reset
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_FILE));
            thresholdInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_THRESHOLD));
            ceilingInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_CEILING));
            logicalChannelGroupRefResponse = getValue(response, LOGICAL_CHANNEL_GROUP_REF);
            assertEquals(origLbThreshold, thresholdInResponse);
            assertEquals(origLbCeiling, ceilingInResponse);
            assertEquals(oriLogicalChannelGroupRef, logicalChannelGroupRefResponse);
    }

    private boolean initialAndTerminationStringsArePresent(String response, NetconfVersion version){
        if (version == NETCONF_1_1) {
            return netconf11StringsArePresent(response);
        } else {
            return response.contains(ONE_DOT_ZERO_TERMINATION);
        }
    }

    private boolean netconf11StringsArePresent(String response) {
        return netconf11StringsArePresent(response, messageStartPattern.matcher(response));
        }

    private boolean netconf11StringsArePresent(String response, Matcher matcher) {
        return matcher.find() && response.substring( matcher.end() ).contains(
                ONE_DOT_ONE_TERMINATION);
    }
}
