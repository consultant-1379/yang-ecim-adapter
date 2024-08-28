/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformerFactory;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfGetIT {

    private static final Logger logger = LoggerFactory.getLogger(NetconfGetIT.class);

    private static final TestProperties props = new TestProperties();

    private static final String EXCEPTION_FORMAT = "Exception thrown while executing {} : {}";

    private XsltTransformer transformer = XsltTransformerFactory.newEcimToYangTransformer();
    private static NetconfSessionsUtil nsu;

    private static final String ENODEB_FUNCTION_XML =
            "<enodeb-function xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">" +
            "   <enb-id>1</enb-id>" +
            "   <enodeb-plmn-id>" +
            "      <mcc>[0-9]+</mcc>" +
            "      <mnc>[0-9]+</mnc>" +
            "      <mnc-length>[2-3]</mnc-length>" +
            "   </enodeb-plmn-id>" +
            "</enodeb-function>";
    private static final String NODE_SUPPORT_XML =
            "<node-support xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-rme-sef-enb-adapter\">" +
            "  <sector-equipment-function>" +
            "    <id>1</id>" +
            "    <administrative-state>.*</administrative-state>" +
            "    <mixed-mode-radio>.*</mixed-mode-radio>" +
            "    <operational-state>.*</operational-state>" +
            "    <rf-branch-ref.*>.*</rf-branch-ref>" +
            "    <eutran-fq-bands.*><utran-fdd-fq-bands.*>" +
            "    <available-hw-output-power>.*</available-hw-output-power>" +
            "    <availability-status>.*</availability-status>" +
            "  </sector-equipment-function>" +
            "</node-support>";

    public final TestName testName = new TestName();

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

    @Ignore
    @Test
    public void testNetconfGetWithNoSubTreeFilterIT(){
        final String ecimFilter =
                "netconf/ecimFilters/ecim-rpc-get-with-filter-ManagedElement-ENodeBFunction.xml";

        try {
            String adapterResult = nsu.getAdapterSession().sendMessage(String.format(RPC_WRAPPER, "0", "<get></get>"));

            String nodeResult = nsu.getNodeSession().sendMessage(String.format(RPC_WRAPPER, "1", getXmlFromFile(ecimFilter)));

            String nodeYang = transformer.transform(stripRpcReplyAndDataTags(nodeResult));

            assertEquals(getXmlWithoutWhitespace(nodeYang),
                    getXmlWithoutWhitespace(stripRpcReplyAndDataTags(adapterResult)));

        } catch (final Exception exception) {
            logger.error("Exception occurred in test!", exception);
            fail();
        }
    }

    @Test
    public void testNetconfGetWithNoSubTreeFilterSameAsGetWithDefaultSubTreeFilterIT(){
        final String yangXmlGetWithFilter = "netconf/yangFilters/in/yang-rpc-get-with-ericsson-lrat-enb-adapter-filter.xml";
        final String ecimXmlGetWithFilter = "netconf/ecimFilters/ecim-rpc-get-with-ericsson-lrat-enb-adapter-filter.xml";

        try {
            String adapterResult = nsu.getAdapterSession().sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(yangXmlGetWithFilter)));

            String nodeEcim = nsu.getNodeSession().sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(ecimXmlGetWithFilter)));
            String nodeYang = transformer.transform(stripRpcReplyAndDataTags(nodeEcim));

            assertEquals(getXmlWithoutWhitespace(nodeYang), getXmlWithoutWhitespace(stripRpcReplyAndDataTags(adapterResult)));

        } catch (final Exception exception) {
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testNetconfGetWithSubTreeFilterLoadBalancingIT(){
        final String yangXmlGetWithFilter = "netconf/yangFilters/in/yang-rpc-get-with-filter-loadbalancing.xml";
        final String ecimXmlGetWithFilter = "netconf/ecimFilters/ecim-rpc-get-with-filter-loadbalancing.xml";

        try {
            String adapterResult = nsu.getAdapterSession().sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(yangXmlGetWithFilter)));

            String nodeEcim = nsu.getNodeSession().sendMessage(String.format(RPC_WRAPPER, "1", getXmlFromFile(ecimXmlGetWithFilter)));
            String nodeYang = transformer.transform(stripRpcReplyAndDataTags(nodeEcim));

            assertEquals(getXmlWithoutWhitespace(nodeYang), getXmlWithoutWhitespace(stripRpcReplyAndDataTags(adapterResult)));

        } catch (final Exception exception) {
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testNetconfGetWithNetconfStateAndEnodeBFilterIT(){

        try {
            String command = getXmlWithoutWhitespace(getXmlFromFile("netconf/netconf-get-with-netconfState-eventStreams-and-enodeb-filter.xml"));

            String result = nsu.getAdapterSession().sendMessage(command);

            Pattern pattern = Pattern.compile(getXmlWithoutWhitespace("<rpc-reply message-id=\"[0-9]+\" " +
                    "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1\\.0\"><data>" +
                    ENODEB_FUNCTION_XML +
                    "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">" +
                    "<capabilities>.*<\\/capabilities>" +
                    "<schemas>.*<\\/schemas>" +
                    "<\\/netconf-state>" +
                    "<netconf xmlns=\"urn:ietf:params:xml:ns:netmod:notification\">" +
                    "    <streams>" +
                    "        <stream>" +
                    "            <name>NETCONF</name>" +
                    "            <description>.*</description>" +
                    "            <replaySupport>false</replaySupport>" +
                    "        </stream>" +
                    "    </streams>" +
                    "</netconf>" +
                    "<\\/data><\\/rpc-reply>"));
            Matcher matcher = pattern.matcher(getXmlWithoutWhitespace(result));
            assertTrue(matcher.matches());

        } catch (final Exception exception) {
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testNetconfGetWithSubTreeFilterContainingBothNodeSupportAndEnodebFunctionIT(){
        final String yangXmlGetWithFilter = "netconf/yangFilters/in/yang-rpc-get-with-filter-node-support-enodeb-function.xml";

        try {
            if (props.isRealNodeTest()) {
                String adapterResult = getXmlWithoutWhitespace(nsu.getAdapterSession().sendMessage(
                        String.format(RPC_WRAPPER, "0", getXmlFromFile(yangXmlGetWithFilter))));

                String nodeSupportXml = getXmlWithoutWhitespace(".*" + NODE_SUPPORT_XML + ".*");
                String enodebFunctionXml = getXmlWithoutWhitespace(".*" + ENODEB_FUNCTION_XML + ".*");

                Pattern patternNodeSupport = Pattern.compile(nodeSupportXml);
                Matcher matcherNodeSupport = patternNodeSupport.matcher(adapterResult);
                assertTrue("Pattern:\n" + nodeSupportXml + "\nnot found in response:\n"+
                        adapterResult, matcherNodeSupport.matches());

                Pattern patternEnodebFunction = Pattern.compile(enodebFunctionXml);
                Matcher matcherEnodebFunction = patternEnodebFunction.matcher(adapterResult);
                assertTrue("Pattern:\n" + enodebFunctionXml + "\nnot found in response:\n"+
                        adapterResult, matcherEnodebFunction.matches());
            }

        } catch (final Exception exception) {
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }
}
