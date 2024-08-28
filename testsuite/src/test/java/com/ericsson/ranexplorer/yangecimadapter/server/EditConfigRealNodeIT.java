/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfVersion.NETCONF_1_1;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getValue;
import static org.junit.Assert.*;

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

public class EditConfigRealNodeIT {

    private static final Logger logger = LoggerFactory.getLogger(EditConfigIT.class);

    private static final String GET_QCI_SUB_QUANTA_NODE = "netconf/editConfigRealNode/getQciSubscriptionQuantaFromNode.xml";
    private static final String EDIT_QCI_SUB_QUANTA = "netconf/editConfigRealNode/editQciSubscriptionQuanta.xml";
    private static final String QCI_SUB_QUANTA = "qciSubscriptionQuanta";
    private static final String GET_ATTRIBUTES_BEFORE_FILE =
            "netconf/editConfigRealNode/getMultipleAttributesFromNode_Before.xml";
    private static final String GET_ATTRIBUTES_AFTER_FILE =
            "netconf/editConfigRealNode/getMultipleAttributesFromNode_After.xml";
    private static final String CREATE_OBJECTS_FILE =
            "netconf/editMultipleOperations/createObjects.xml";
    private static final String EDIT_MULTIPLE_OPERATIONS_FILE =
            "netconf/editConfigRealNode/editMultipleAttributes.xml";
    private static final String RESET_EDIT_MULTIPLE_FILE =
            "netconf/editConfigRealNode/resetMultipleAttributes.xml";
    private static final String LB_THRESHOLD = "lbThreshold";
    private static final String LB_CEILING = "lbCeiling";
    private static final String DL_MAX_WAITING_TIME = "dlMaxWaitingTime";
    private static final String QCI_PROFILE_PREDEFINED_ID = "qci9";
    private static final String TAC = "tac";
    private static final String OK_XML_TAG = "<ok/>";
    private static final String ONE_DOT_ZERO_TERMINATION = "]]>]]>";
    private static final String ONE_DOT_ONE_TERMINATION = "\n##\n";
    private static final Pattern messageStartPattern = Pattern.compile("#[0-9]+\n");

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
        try {
            updateSingleAttributeWithAssertions(nsu.getAdapterSession(), nsu.getNodeSession());
        } catch (final Exception exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    private void updateSingleAttributeWithAssertions(NetconfSession adapterNetconfSession,
                                                     NetconfSession nodeNetconfSession) {
        String response;

        //Get current value of qciSubscriptionQuanta from Node or via adapter
        response = nodeNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NODE, QCI_PROFILE_PREDEFINED_ID));
        final int origQciSubscriptionQuanta = Integer.parseInt(getValue(response, QCI_SUB_QUANTA));

        // Set the new value for qciSubscriptionQuanta using the adapter
        final int newSubscriptionQuantaValue = origQciSubscriptionQuanta + 1;
        response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, QCI_PROFILE_PREDEFINED_ID, Integer.toString(newSubscriptionQuantaValue)));
        assertTrue(response.contains(OK_XML_TAG));
        assertTrue( initialAndTerminationStringsArePresent( response, adapterNetconfSession
                .getVersion() ) );

        // Confirm new value is set in Node
        response = nodeNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NODE, QCI_PROFILE_PREDEFINED_ID));
        int result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        assertEquals(newSubscriptionQuantaValue, result);

        // Reset value back to original
        response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, QCI_PROFILE_PREDEFINED_ID, Integer.toString(origQciSubscriptionQuanta)));
        assertTrue(response.contains(OK_XML_TAG));
        assertTrue( initialAndTerminationStringsArePresent( response, adapterNetconfSession
                .getVersion() ) );

        // Check value is reset
        response = nodeNetconfSession.sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NODE, QCI_PROFILE_PREDEFINED_ID));
        result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        assertEquals(origQciSubscriptionQuanta, result);
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

    @Test
    public void testNetconf11EditConfig_updateSingleAttribute_attributeUpdated() { //NOSONAR
        try {
            updateSingleAttributeWithAssertions(nsu.getAdapterSession(), nsu.getNodeSession());
        } catch (final Exception exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testEditConfig_useAllSubOperations_allOperationsSucceeded() { //NOSONAR
        try {
            String response;

            //Get current values from Node
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_BEFORE_FILE));
            final int origLbThreshold = Integer.parseInt(getValue(response, LB_THRESHOLD));
            final int origLbCeiling = Integer.parseInt(getValue(response, LB_CEILING));
            final int oriDlMaxWaitingTime = Integer.parseInt(getValue(response, DL_MAX_WAITING_TIME));

            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_OBJECTS_FILE));
            assertTrue(response.contains(OK_XML_TAG));

            // Send edit-config with multiple operations to the adapter
            final int newLbThresholdValue = origLbThreshold + 1;
            final int newLbCeilingValue = origLbCeiling + 1;
            final int newDlMaxWaitingTime = oriDlMaxWaitingTime + 1;
            final String tacValue = "4000";
            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(EDIT_MULTIPLE_OPERATIONS_FILE,
                    tacValue, Integer.toString(newLbThresholdValue), Integer.toString(newLbCeilingValue),
                    Integer.toString(newDlMaxWaitingTime)));
            assertTrue(response.contains(OK_XML_TAG));

            // Confirm new values are set in Node
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_AFTER_FILE));
            int thresholdInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_THRESHOLD));
            int ceilingInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_CEILING));
            int dlMaxWaitingTimeResponse = Integer.parseInt(XmlHelper.getValue(response, DL_MAX_WAITING_TIME));
            String tacResponse = XmlHelper.getValue(response, TAC);
            assertEquals(newLbThresholdValue, thresholdInResponse);
            assertEquals(newLbCeilingValue, ceilingInResponse);
            assertEquals(newDlMaxWaitingTime, dlMaxWaitingTimeResponse);
            assertEquals(tacValue, tacResponse);

            // Reset value back to original
            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(RESET_EDIT_MULTIPLE_FILE,
                    Integer.toString(origLbThreshold), Integer.toString(origLbCeiling), Integer.toString(oriDlMaxWaitingTime)));
            assertTrue(response.contains(OK_XML_TAG));

            // Check values are reset
            response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_BEFORE_FILE));
            thresholdInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_THRESHOLD));
            ceilingInResponse = Integer.parseInt(XmlHelper.getValue(response, LB_CEILING));
            dlMaxWaitingTimeResponse = Integer.parseInt(XmlHelper.getValue(response, DL_MAX_WAITING_TIME));
            assertEquals(origLbThreshold, thresholdInResponse);
            assertEquals(origLbCeiling, ceilingInResponse);
            assertEquals(oriDlMaxWaitingTime, dlMaxWaitingTimeResponse);
        } catch (final Exception exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }
}
