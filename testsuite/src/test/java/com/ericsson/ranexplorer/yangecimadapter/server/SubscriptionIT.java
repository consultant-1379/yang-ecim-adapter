/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ADAPTER;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.util.*;

public class SubscriptionIT {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionIT.class);
    private static final TestProperties props = new TestProperties();

    private static final String LB_THRESHOLD = "lbThreshold";
    private static final String LB_CEILING = "lbCeiling";
    private static final String DL_MAX_WAITING_TIME = "dlMaxWaitingTime";
    private static final String CONFIGURED_MAX_TX_POWER = "configuredMaxTxPower";
    private static final String MIXED_MODE_RADIO = "mixedModeRadio";
    private static final String OK_XML_TAG = "<ok/>";
    private static final String DEFAULT_MESSAGE_ID = "1";

    private static final String CREATE_SUBSCRIPTION_NO_FILTER = "netconf/notifications/create-subscription-no-filter.xml";
    private static final String CREATE_SUBSCRIPTION_FILTER = "netconf/notifications/create-subscription-with-filter.xml";
    private static final String CREATE_SUBSCRIPTION_LOAD_BALANCING_FILTER = "netconf/notifications/create-subscription-with-filter-load-balancing-function.xml";
    private static final String CREATE_SUBSCRIPTION_SECTOR_CARRIER_SECTOR_EQUIPMENT_FUNCTION_FILTER
            = "netconf/notifications/create-subscription-with-filter-sector-carrier.xml";
    private static final String GET_LBCEILING_ECIM = "netconf/notifications/get-load-balancing-ecim.xml";
    private static final String EDIT_CONFIG_LBCEILING_ECIM = "netconf/notifications/edit-config-lbceiling-ecim.xml";
    private static final String GET_ATTRIBUTES_ECIM = "netconf/notifications/get-node-attributes-ecim.xml";
    private static final String EDIT_CONFIG_ATTRIBUTES_ECIM = "netconf/notifications/edit-config-node-attributes-ecim.xml";
    private static final String EDIT_CONFIG_SECTOR_CARRIER_SECTOR_EQUIPMENT_FUNCTION_ECIM = "netconf/notifications/edit-config-sector-carrier-sector-equipment-function-ecim.xml";

    // keeping SONAR happy
    private static final String XML_EDIT = "<edit>";
    private static final String XML_TARGET = "<target xmlns:lrtadpt=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">";
    private static final String XML_TARGET_RSEF = "<target xmlns:rsefadpt=\"urn:rdns:com:ericsson:oammodel:ericsson-rme-sef-enb-adapter\">";
    private static final String XML_TARGET_END = "</target>";
    private static final String LB_CEILING_VALUE = "/lrtadpt:enodeb-function/lrtadpt:load-balancing-function/lrtadpt:lb-ceiling";
    private static final String EXCEPTION_FORMAT = "Exception occurred in test: {}";

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
    public void openSessions() {
        try {
            nsu.openSessions();
        }catch (IOException exception){
            logger.error("Exception thrown while creating sessions with error [{}]", exception.getMessage(), exception);
            fail();
        }
    }

    @After
    public void closeSessions() {
        nsu.closeSessions();
    }

    @Test
    public void testCreateSubscriptionWithDefaultFilterSucceeds() {
        try {
            if (props.isRealNodeTest()) {
                String response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_SUBSCRIPTION_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        }
    }

    @Test
    public void testCreateSubscriptionWithoutFilterSucceeds() {
        try {
            if (props.isRealNodeTest()) {
                String response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_SUBSCRIPTION_NO_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        }
    }

    @Test
    public void testCreateSubscriptionOnSameSessionReturnsError() {
        String secondMessageId = "2";
        try {
            if (props.isRealNodeTest()) {
                String response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_SUBSCRIPTION_NO_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));

                response = nsu.getAdapterSession().sendMessage(getXmlFromFile(CREATE_SUBSCRIPTION_NO_FILTER, secondMessageId));
                Pattern pattern = Pattern.compile(".*<rpc-reply " +
                        "message-id=\"" + secondMessageId + "\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "<rpc-error>" +
                        "<error-type>protocol</error-type>" +
                        "<error-tag>operation-failed</error-tag>" +
                        "<error-severity>error</error-severity>" +
                        ".*" +
                        "</rpc-error>" +
                        "</rpc-reply>");
                Matcher matcher = pattern.matcher(getXmlWithoutWhitespace(response));
                assertTrue(matcher.matches());
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        }
    }

    @Test
    public void testCreateSubscriptionWithFilterThatNotificationReceived() {
        final String nodeMessageId = "3";
        NetconfSession adapterSession = null;

        try {
            if (props.isRealNodeTest()) {
                adapterSession = new NetconfSession(ADAPTER, props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterSession.open();

                String response = adapterSession.sendMessage(
                        getXmlFromFile(CREATE_SUBSCRIPTION_LOAD_BALANCING_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));

                NetconfNotificationHandler netconfNotificationHandler = new NetconfNotificationHandler(adapterSession);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_LBCEILING_ECIM, nodeMessageId));
                final int origLbCeiling = Integer.parseInt(getValue(response, LB_CEILING));
                final int newLbCeiling = origLbCeiling + 1;

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_LBCEILING_ECIM,
                        nodeMessageId, Integer.toString(newLbCeiling)));
                assertTrue(response.contains(OK_XML_TAG));

                String notificationPattern = "<notification " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">" +
                        "<eventTime>.*</eventTime>" +
                        "<netconf-config-change xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-notifications\">" +
                        "<changed-by><server/></changed-by>" +
                        "<datastore>running</datastore>" +
                        XML_EDIT +
                        XML_TARGET +
                        LB_CEILING_VALUE +
                        XML_TARGET_END +
                        "<operation>merge</operation>" +
                        "</edit>" +
                        "</netconf-config-change>" +
                        "</notification>";
                assertTrue(netconfNotificationHandler.isNotificationReceived(notificationPattern));

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_LBCEILING_ECIM,
                        nodeMessageId, Integer.toString(origLbCeiling)));
                assertTrue(response.contains(OK_XML_TAG));

                netconfNotificationHandler.end();
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        } finally {
            closeSession(adapterSession);
        }
    }

    @Test
    public void testCreateSubscriptionWithNoFilterThatAllNotificationsReceived() {
        final String nodeMessageId = "4";
        NetconfSession adapterSession = null;

        try {
            if (props.isRealNodeTest()) {
                adapterSession = new NetconfSession(ADAPTER, props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterSession.open();

                String response = adapterSession.sendMessage(getXmlFromFile(CREATE_SUBSCRIPTION_NO_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));

                NetconfNotificationHandler netconfNotificationHandler = new NetconfNotificationHandler(adapterSession);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_ECIM, nodeMessageId));
                final String origLbCeiling = getValue(response, LB_CEILING);
                final String origLbThreshold = getValue(response, LB_THRESHOLD);
                final String oriDlMaxWaitingTime = getValue(response, DL_MAX_WAITING_TIME);

                final String newLbCeiling = Integer.toString(Integer.parseInt(origLbThreshold) + 1);
                final String newLbThreshold = Integer.toString(Integer.parseInt(origLbThreshold) + 1);
                final String newDlMaxWaitingTime = Integer.toString(Integer.parseInt(oriDlMaxWaitingTime) + 1);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_ATTRIBUTES_ECIM,
                        nodeMessageId, newLbThreshold, newLbCeiling, newDlMaxWaitingTime));
                assertTrue(response.contains(OK_XML_TAG));

                String patternLbThreshold = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        "/lrtadpt:enodeb-function/lrtadpt:load-balancing-function/lrtadpt:lb-threshold" +
                        XML_TARGET_END +
                        ".*";
                String patternLbCeiling = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        LB_CEILING_VALUE +
                        XML_TARGET_END +
                        ".*";
                String patternDlMaxWaitingTime = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        "/lrtadpt:enodeb-function/lrtadpt:qci-table/lrtadpt:qci-profile-operator-defined\\[lrtadpt:id='shin1'\\]/lrtadpt:dl-max-waiting-time" +
                        XML_TARGET_END +
                        ".*";

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbThreshold));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbCeiling));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternDlMaxWaitingTime));

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_ATTRIBUTES_ECIM,
                        nodeMessageId, origLbThreshold, origLbCeiling, oriDlMaxWaitingTime));
                assertTrue(response.contains(OK_XML_TAG));

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbThreshold));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbCeiling));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternDlMaxWaitingTime));
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        } finally {
            closeSession(adapterSession);
        }
    }

    @Test
    public void testCreateSubscriptionWithLoadBalancingFilterAndOnlyLoadBalancingNotificationsReceived() {
        final String nodeMessageId = "5";
        NetconfSession adapterSession = null;

        try {
            if (props.isRealNodeTest()) {
                adapterSession = new NetconfSession(ADAPTER, props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterSession.open();

                String response = adapterSession.sendMessage(
                        getXmlFromFile(CREATE_SUBSCRIPTION_LOAD_BALANCING_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));

                NetconfNotificationHandler netconfNotificationHandler = new NetconfNotificationHandler(adapterSession);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_ECIM, nodeMessageId));
                final String origLbCeiling = getValue(response, LB_CEILING);
                final String origLbThreshold = getValue(response, LB_THRESHOLD);
                final String oriDlMaxWaitingTime = getValue(response, DL_MAX_WAITING_TIME);

                final String newLbCeiling = Integer.toString(Integer.parseInt(origLbThreshold) + 1);
                final String newLbThreshold = Integer.toString(Integer.parseInt(origLbThreshold) + 1);
                final String newDlMaxWaitingTime = Integer.toString(Integer.parseInt(oriDlMaxWaitingTime) + 1);

                logger.debug("Changing {}: {} -> {}, {}: {} -> {}, {}: {} -> {}", LB_CEILING, origLbCeiling, newLbCeiling,
                        LB_THRESHOLD, origLbThreshold, newLbCeiling, DL_MAX_WAITING_TIME, oriDlMaxWaitingTime, newDlMaxWaitingTime);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_ATTRIBUTES_ECIM,
                        nodeMessageId, newLbThreshold, newLbCeiling, newDlMaxWaitingTime));
                assertTrue(response.contains(OK_XML_TAG));

                String patternLbThreshold = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        "/lrtadpt:enodeb-function/lrtadpt:load-balancing-function/lrtadpt:lb-threshold" +
                        XML_TARGET_END +
                        ".*";
                String patternLbCeiling = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        LB_CEILING_VALUE +
                        XML_TARGET_END +
                        ".*";
                String patternDlMaxWaitingTime = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        "/lrtadpt:enodeb-function/lrtadpt:qci-table/lrtadpt:qci-profile-operator-defined\\[lrtadpt:id='shin1'\\]/lrtadpt:dl-max-waiting-time" +
                        XML_TARGET_END +
                        ".*";

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbThreshold));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbCeiling));
                assertFalse(netconfNotificationHandler.isNotificationReceived(patternDlMaxWaitingTime));

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_ATTRIBUTES_ECIM,
                        nodeMessageId, origLbThreshold, origLbCeiling, oriDlMaxWaitingTime));
                assertTrue(response.contains(OK_XML_TAG));

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbThreshold));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternLbCeiling));
                assertFalse(netconfNotificationHandler.isNotificationReceived(patternDlMaxWaitingTime));

                netconfNotificationHandler.end();
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        } finally {
            closeSession(adapterSession);
        }
    }

    @Test
    public void testMultipleSessionsAndSubscriptionsReceiveNotifications() {
        final String nodeMessageId = "6";

        NetconfSession netconfSession1 = null;
        NetconfSession netconfSession2 = null;
        try {
            if (props.isRealNodeTest()) {
                netconfSession1 = new NetconfSession("subscription-test-1", props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                netconfSession1.open();
                assertTrue(netconfSession1.channelIsOpen());

                netconfSession2 = new NetconfSession("subscription-test-2", props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                netconfSession2.open();
                assertTrue(netconfSession2.channelIsOpen());

                String response = netconfSession1.sendMessage(
                        getXmlFromFile(CREATE_SUBSCRIPTION_LOAD_BALANCING_FILTER, "1"));
                assertTrue(response.contains(OK_XML_TAG));
                NetconfNotificationHandler nnh1 = new NetconfNotificationHandler(netconfSession1);

                response = netconfSession2.sendMessage(
                        getXmlFromFile(CREATE_SUBSCRIPTION_LOAD_BALANCING_FILTER, "2"));
                assertTrue(response.contains(OK_XML_TAG));
                NetconfNotificationHandler nnh2 = new NetconfNotificationHandler(netconfSession2);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_LBCEILING_ECIM, nodeMessageId));
                final int origLbCeiling = Integer.parseInt(getValue(response, LB_CEILING));
                final int newLbCeiling = origLbCeiling + 1;

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_LBCEILING_ECIM,
                        nodeMessageId, Integer.toString(newLbCeiling)));
                assertTrue(response.contains(OK_XML_TAG));

                String pattern = "<notification " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">" +
                        "<eventTime>.*</eventTime>" +
                        "<netconf-config-change xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-notifications\">" +
                        "<changed-by><server/></changed-by>" +
                        "<datastore>running</datastore>" +
                        XML_EDIT +
                        XML_TARGET +
                        LB_CEILING_VALUE +
                        XML_TARGET_END +
                        "<operation>merge</operation>" +
                        "</edit>" +
                        "</netconf-config-change>" +
                        "</notification>";

                assertTrue(nnh1.isNotificationReceived(pattern));
                assertTrue(nnh2.isNotificationReceived(pattern));

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_LBCEILING_ECIM,
                        nodeMessageId, Integer.toString(origLbCeiling)));
                assertTrue(response.contains(OK_XML_TAG));

                nnh1.end();
                nnh2.end();
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        } finally {
            closeSession(netconfSession1, netconfSession2);
        }
    }

    @Test
    public void testGetStreamsRequestReturnsDefaultNetconfStream() {
        try {
            if (props.isRealNodeTest()) {
                String response = nsu.getAdapterSession().sendMessage(
                        getXmlFromFile("netconf/notifications/get-streams.xml", "1"));

                Pattern pattern = Pattern.compile(".*<stream><name>NETCONF</name>.*<replaySupport>.*");
                Matcher matcher = pattern.matcher(XmlHelper.getXmlWithoutWhitespace(response));
                assertTrue(matcher.matches());
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        }
    }

    @Test
    public void testCreateSubscriptionWithSectorCarrierFilterAndOnlySectorCarrierNotificationsReceived() {
        final String nodeMessageId = "7";
        NetconfSession adapterSession = null;

        try {
            if (props.isRealNodeTest()) {
                adapterSession = new NetconfSession(ADAPTER, props.valueOf(ADAPTER_HOST),
                    props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterSession.open();

                String response = adapterSession.sendMessage(
                        getXmlFromFile(CREATE_SUBSCRIPTION_SECTOR_CARRIER_SECTOR_EQUIPMENT_FUNCTION_FILTER, DEFAULT_MESSAGE_ID));
                assertTrue(response.contains(OK_XML_TAG));

                NetconfNotificationHandler netconfNotificationHandler = new NetconfNotificationHandler(adapterSession);

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(GET_ATTRIBUTES_ECIM, nodeMessageId));
                final String oriConfiguredMaxTxPower = getValue(response, CONFIGURED_MAX_TX_POWER);
                final String oriMixedModeRadio = getValue(response, MIXED_MODE_RADIO);

                final String newConfiguredMaxTxPower = Integer.toString(Integer.parseInt(oriConfiguredMaxTxPower) + 10000);
                final String newMixedModeRadio = "true".equals(oriMixedModeRadio) ? "false" : "true";

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_SECTOR_CARRIER_SECTOR_EQUIPMENT_FUNCTION_ECIM,
                        nodeMessageId, newConfiguredMaxTxPower, newMixedModeRadio));
                assertTrue(response.contains(OK_XML_TAG));

                String patternConfiguredMaxTxPower = ".*" +
                        XML_EDIT +
                        XML_TARGET +
                        "/lrtadpt:enodeb-function/lrtadpt:sector-carrier\\[lrtadpt:id='2'\\]/lrtadpt:configured-max-tx-power" +
                        XML_TARGET_END +
                        ".*";

                String patternMixedModeRadio = ".*" +
                        XML_EDIT +
                        XML_TARGET_RSEF +
                        "/rsefadpt:node-support/rsefadpt:sector-equipment-function\\[rsefadpt:id='1'\\]/rsefadpt:mixed-mode-radio" +
                        XML_TARGET_END +
                        ".*";

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternConfiguredMaxTxPower));
                assertTrue("No notification matches following pattern:" + patternMixedModeRadio,
                        netconfNotificationHandler.isNotificationReceived(patternMixedModeRadio));

                response = nsu.getNodeSession().sendMessage(getXmlFromFile(EDIT_CONFIG_SECTOR_CARRIER_SECTOR_EQUIPMENT_FUNCTION_ECIM,
                        nodeMessageId, oriConfiguredMaxTxPower, oriMixedModeRadio));
                assertTrue(response.contains(OK_XML_TAG));

                assertTrue(netconfNotificationHandler.isNotificationReceived(patternConfiguredMaxTxPower));
                assertTrue(netconfNotificationHandler.isNotificationReceived(patternMixedModeRadio));

                netconfNotificationHandler.end();
            }
        } catch (Exception exception){
            logger.error(EXCEPTION_FORMAT, testName.getMethodName(), exception);
            fail();
        } finally {
            closeSession(adapterSession);
        }
    }

    private void closeSession(NetconfSession... netconfSessions){
        for(NetconfSession session: netconfSessions) {
            if (session != null) {
                session.closeAll();
            }
        }
    }
}
