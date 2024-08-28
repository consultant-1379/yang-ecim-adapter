/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfVersion.NETCONF_1_1;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getValue;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getXmlWithoutWhitespace;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class AdapterPrefixIT {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterPrefixIT.class);

    private static final String ECIM_INPUT_TO_NETSIM = "netconf/getEnodeBIdEcim.xml";
    private static final String GET_QCI_SUB_QUANTA_NETSIM = "netconf/getQciSubscriptionQuantaFromNode.xml";
    private static final String QCI_SUB_QUANTA = "qciSubscriptionQuanta";
    private static final String QCI_PROFILE_PREDEFINED_ID = "9";

    private static final Pattern messageStartPattern = Pattern.compile("#[0-9]+\n");

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
    public void testGetWithNoPrefixes() {
        final String getNoPrefixInput = "netconf/prefixes/get/noPrefix.xml";

        String expected = getNetsimQciQuantaValue();
        String actual = getAdapterQciQuantaValue(getNoPrefixInput, true);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetWithInnerMOPrefixOnly() {
        final String getInnerMoPrefixInput = "netconf/prefixes/get/innerMOPrefix.xml";

        String expected = getNetsimQciQuantaValue();
        String actual = getAdapterQciQuantaValue(getInnerMoPrefixInput, true);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetWithRpcPrefixOnly() {
        final String getRpcPrefixOnlyInput = "netconf/prefixes/get/rpcPrefixOnly.xml";

        String expected = getNetsimQciQuantaValue();
        String actual = getAdapterQciQuantaValue(getRpcPrefixOnlyInput, false);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetWithPrefixOnAll() {
        final String getPrefixOnAllInput = "netconf/prefixes/get/prefixOnAll.xml";

        String expected = getNetsimQciQuantaValue();
        String actual = getAdapterQciQuantaValue(getPrefixOnAllInput, false);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetWithPrefixOnAllAndNamespaceDefinedInRoot() {
        final String getWithPrefixAndNamespacesInRootTagInput = "netconf/prefixes/get/rpcPrefixWithNamespaceInRoot.xml";

        String expected = getNetsimQciQuantaValue();
        String actual = getAdapterQciQuantaValue(getWithPrefixAndNamespacesInRootTagInput, false);
        assertEquals(expected, actual);
    }

    @Test
    public void testEditConfigWithNoPrefix(){
        String editConfigNoPrefixInput = "netconf/prefixes/editConfig/noPrefix.xml";
        updateSingleAttributeWithAssertions(editConfigNoPrefixInput);
    }

    @Test
    public void testEditConfigWithInnerMoPrefixOnly(){
        String editConfigInnerMoPrefixOnlyInput = "netconf/prefixes/editConfig/innerMoPrefix.xml";
        updateSingleAttributeWithAssertions(editConfigInnerMoPrefixOnlyInput);
    }

    @Test
    public void testEditConfigWithRpcPrefixOnly(){
        final String editConfigRpcPrefixOnlyInput = "netconf/prefixes/editConfig/rpcPrefixOnly.xml";
        updateSingleAttributeWithAssertions(editConfigRpcPrefixOnlyInput);
    }

    @Test
    public void testEditConfigWithPrefixOnAll(){
        final String editConfigPrefixOnAllInput = "netconf/prefixes/editConfig/prefixOnAll.xml";
        updateSingleAttributeWithAssertions(editConfigPrefixOnAllInput);
    }

    @Test
    public void testEditConfigWithPrefixOnAllAndNamespaceDefinedInRoot(){
        final String editConfigPrefixAndNamespaceInRootTagInput = "netconf/prefixes/editConfig/rpcPrefixWithNamespacesInRoot.xml";
        updateSingleAttributeWithAssertions(editConfigPrefixAndNamespaceInRootTagInput);
    }

    @Test
    public void testEditConfigWithPrefixAndMultipleNamespaceDef(){
        final String adapterInput = "netconf/prefixes/editConfig/innerMoPrefixWithMultiNamespaceDefinition.xml";
        updateSingleAttributeWithAssertions(adapterInput);
    }

    private String getNetsimQciQuantaValue() {
        final String netsimResponse = nsu.getNodeSession().sendMessage(String.format(RPC_WRAPPER, "1", getXmlWithoutWhitespace(getXmlFromFile(ECIM_INPUT_TO_NETSIM))));
        return XmlHelper.getValue(getXmlWithoutWhitespace(netsimResponse), "eNBId");
    }

    private String getAdapterQciQuantaValue(String inputFile, boolean format) {
        String input;
        if (format) {
            input = String.format(RPC_WRAPPER, "2", getXmlWithoutWhitespace(getXmlFromFile(inputFile)));
        } else {
            input = getXmlWithoutWhitespace(getXmlFromFile(inputFile));
        }
        final String adapterResponse = nsu.getAdapterSession().sendMessage(input);
        return XmlHelper.getValue(getXmlWithoutWhitespace(adapterResponse), "enb-id");
    }

    private void updateSingleAttributeWithAssertions(final String inputFile) {

        String response;

        //Get current value of qciSubscriptionQuanta from Netsim or via adapter
        response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        final int origQciSubscriptionQuanta = Integer.parseInt(getValue(response, QCI_SUB_QUANTA));
        LOG.info("Orginal value from netsim: {}", origQciSubscriptionQuanta);

        // Set the new value for qciSubscriptionQuanta using the adapter
        final int newSubscriptionQuantaValue = origQciSubscriptionQuanta + 1;
        response = nsu.getAdapterSession().sendMessageAndGetRawResponse(getXmlFromFile(inputFile, Integer.toString(newSubscriptionQuantaValue)));
        assertTrue(response.contains(OK_XML_TAG));
        LOG.info("Setting new value: {}", newSubscriptionQuantaValue);
        assertTrue(initialAndTerminationStringsArePresent(response, nsu.getAdapterSession()
                .getVersion()));

        // Confirm new value is set in Netsim
        response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        int result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        LOG.info("Check: Set value {} and result value {} ", newSubscriptionQuantaValue, result);
        assertEquals(newSubscriptionQuantaValue, result);

        // Reset value back to original
        response = nsu.getAdapterSession().sendMessageAndGetRawResponse(getXmlFromFile(inputFile, Integer.toString(origQciSubscriptionQuanta)));
        assertTrue(response.contains(OK_XML_TAG));
        assertTrue(initialAndTerminationStringsArePresent(response, nsu.getAdapterSession()
                .getVersion()));

        // Check value is reset
        response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_QCI_SUB_QUANTA_NETSIM, QCI_PROFILE_PREDEFINED_ID));
        result = Integer.parseInt(XmlHelper.getValue(response, QCI_SUB_QUANTA));
        LOG.info("Check value reset to original: original {} : reset value : {} ", origQciSubscriptionQuanta, result);
        assertEquals(origQciSubscriptionQuanta, result);
    }

    private boolean initialAndTerminationStringsArePresent(String response, NetconfVersion version) {
        if (version == NETCONF_1_1) {
            return netconf11StringsArePresent(response);
        } else {
            return response.contains(ONE_DOT_ZERO_TERMINATION);
        }
    }

    private boolean netconf11StringsArePresent(String response) {
        Matcher matcher = messageStartPattern.matcher(response);
        return  matcher.find() && response.substring(matcher.end()).contains(ONE_DOT_ONE_TERMINATION);
    }
}
