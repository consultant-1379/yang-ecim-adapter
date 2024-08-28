/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.constants.TestConstants.*;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.filter.SubTreeFilter;
import com.ericsson.oss.mediation.util.netconf.manager.NetconfManagerStub;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MessageState;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MetricsHandler;

public class AdapterListenerTest {

    public static final String MESSAGE_ID = "101";

    private static final Logger LOG = LoggerFactory.getLogger(AdapterListenerTest.class);
    private static final String VERSION110_END_TAG = "]]>]]>";
    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;

    @Rule
    public TestName testName = new TestName();

    private AdapterListener adapterListener;
    private TransportManagerStub transportManagerStub = new TransportManagerStub();

    private StringWriter out;
    private PrintWriter writer;
    private int sessionId = 1;

    @Before
    public void setUp() throws NetconfManagerException {
        out = new StringWriter();
        writer = new PrintWriter(out);
        adapterListener = Mockito.spy(AdapterListener.class);
        final NetconfManager netconfManager = new NetconfManagerStub(transportManagerStub);
        adapterListener.setNetconfManager(netconfManager);
        METRICS_HANDLER.markStartCreateSession(sessionId);
        METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_REQUEST);
        adapterListener.setSessionId(sessionId);
    }

    @Test
    public void testHello() {
        adapterListener.hello(sessionId, writer);
        writer.flush();

        final String result = removeWhiteSpace(out.toString());
        assertEquals(EXPECTED_HELLO_RESULT, result);
    }

    @Test
    public void testGetWithNullFilterDefaultFilterIsUsed() {
        try {
            adapterListener.get(MESSAGE_ID, null, writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_NULL_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetWithEmptyEnodeBFunctionDefaultFilterIsUsed() {
        try {
            adapterListener.get(MESSAGE_ID, new SubTreeFilter(EMPTY_ENODEB_FUNCTION_FILTER), writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_ENODEBFUNCTION_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetFilterHasEmptyEnodeBFunctionWithWhiteSpaceDefaultFilterIsUsed() {//NOSONAR
        try {
            adapterListener.get(MESSAGE_ID, new SubTreeFilter("<enodeb-function>   \n\n \t \t    \n</enodeb-function>"), writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_ENODEBFUNCTION_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetWithEmptyFilter() {

        final SubTreeFilter subTreeFilter = new SubTreeFilter(" ");
        adapterListener.get(MESSAGE_ID, subTreeFilter, writer);
        writer.flush();

        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_EMPTY_FILTER_RESULT, actualResult);
    }

    @Test
    public void testGetWithValidFilter() {

        try {
            final SubTreeFilter inputFilter = new SubTreeFilter(VALID_FILTER);
            adapterListener.get(MESSAGE_ID, inputFilter, writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(removeWhiteSpace(EXPECTED_VALID_FILTER_RESULT), actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetConfigWithNullFilterDefaultFilterIsUsed() {

        try {
            adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, null, writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_NULL_FILTER_CONFIG_DATA_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetConfig_datastoreIsCandidate_ErrorReturned() { //NOSONAR

        try {
            adapterListener.getConfig(MESSAGE_ID, Datastore.CANDIDATE, null, writer);
            writer.flush();

            final String result = removeEndTag(removeWhiteSpace(out.toString()));
            assertTrue("Error indicating that candidate datastore is not supported should have been returned", result.contains("<rpc-error>"));
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGet_filterContainsMalformedXml_errorMessageReceived() { //NOSONAR

        adapterListener.get(MESSAGE_ID, new SubTreeFilter(INVALID_FILTER), writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));

        assertTrue("Response should contain " + RPC_ERROR, actualResult.contains(RPC_ERROR));
    }

    @Test
    public void testGetReturningAnError() {

        final SubTreeFilter filter = new SubTreeFilter(VALID_FILTER_INVALID_DATA);

        adapterListener.get(MESSAGE_ID, filter, writer);
        writer.flush();
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_INVALID_DATA_RESULT, actualResult);

    }

    @Test
    public void testGetWithEmptyNetconfStateFilter() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EMPTY_NETCONFSTATE);
        adapterListener.get(MESSAGE_ID, filter, writer);
        writer.flush();
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        final String expectedResult = String.format(EXPECTED_NETCONF_STATE_REPLY, EXPECTED_CAPABILITIES + EXPECTED_DATASTORES + EXPECTED_SCHEMA);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetWithNetconfStateCapabilities() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_NETCONFSTATE_CAPABILITIES);
        adapterListener.get(MESSAGE_ID, filter, writer);
        writer.flush();
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        final String expectedResult = String.format(EXPECTED_NETCONF_STATE_REPLY, EXPECTED_CAPABILITIES);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetConfigWithEmptyNetconfStateFilter() {
        try {
            adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, new SubTreeFilter(FILTER_WITH_EMPTY_NETCONFSTATE), writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_EMPTY_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetConfigWithNetconfStateCapabilities() {
        try {
            adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, new SubTreeFilter(FILTER_WITH_NETCONFSTATE_CAPABILITIES), writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_EMPTY_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetWithNetconfStateCapabilitiesAndENodeBFunctionFilter() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_NETCONFSTATE_CAPABILITIES_AND_ENODEBFUNCTION);
        adapterListener.get(MESSAGE_ID, filter, writer);
        writer.flush();
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_CAPABILITIRS_EMPTY_ENODEBFUNCTION_RESULT, actualResult);
    }

    @Test
    public void testGetConfigWithNetconfStateCapabilitiesAndENodeBFunctionFilter() {
        try {
            adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, new SubTreeFilter(FILTER_WITH_NETCONFSTATE_CAPABILITIES_AND_ENODEBFUNCTION),
                    writer);
            writer.flush();

            final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
            assertEquals(EXPECTED_ENODEBFUNCTION_FILTER_RESULT, actualResult);
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testGetWithEventStreamsFilter() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EVENT_STREAMS);
        adapterListener.get(MESSAGE_ID, filter, writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_EVENT_STREAMS_RESULT, actualResult);
    }

    @Test
    public void testGetWithEventStreamsAndENodeBFunctionFilter() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EVENT_STREAMS_AND_ENODEBFUNCTION);
        adapterListener.get(MESSAGE_ID, filter, writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_EVENT_STREAMS_EMPTY_ENODEBFUNCTION_RESULT, actualResult);
    }

    @Test
    public void testGetWithEventStreamsAndNetconfStateAndENodeBFunctionFilter() {
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EVENT_STREAMS_NETCONF_STATE_AND_ENODEBFUNCTION);
        adapterListener.get(MESSAGE_ID, filter, writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        final String expectedResult = String.format(EXPECTED_EVENT_STREAMS_NETCONF_STATE_EMPTY_ENODEBFUNCTION_RESULT, EXPECTED_CAPABILITIES + EXPECTED_DATASTORES + EXPECTED_SCHEMA);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetConfig_filterWithOnlyEventStreams_emptyDataReturned() { //NOSONAR
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EVENT_STREAMS);
        adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, filter, writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_EMPTY_FILTER_RESULT, actualResult);
    }

    @Test
    public void testGetConfig_filterWithEventStreamsAndENodeBFunction_onlyENodeBFunctionDataReturned() { //NOSONAR
        final SubTreeFilter filter = new SubTreeFilter(FILTER_WITH_EVENT_STREAMS_AND_ENODEBFUNCTION);
        adapterListener.getConfig(MESSAGE_ID, Datastore.RUNNING, filter, writer);
        final String actualResult = removeEndTag(removeWhiteSpace(out.toString()));
        assertEquals(EXPECTED_ENODEBFUNCTION_FILTER_RESULT, actualResult);
    }

    @Test
    public void testGet_filterWithEmptyEquipment_onlyDefaultEquipmentFilterDataReturned(){ //NOSONAR
        final SubTreeFilter emptyEquipmentFilter = new SubTreeFilter("<equipment/>");
        adapterListener.get(MESSAGE_ID, emptyEquipmentFilter, writer);
        final String actualResult = out.toString();
        assertEquals(removeWhiteSpace(EXPECTED_EMPTY_EQUIPMENT_FILTER_RESULT), removeEndTag(removeWhiteSpace(actualResult)));
    }

    @Test
    public void testAvcNotification() {
        try {
            Callable<Boolean> notificationTransformer = adapterListener.createSubscription(MESSAGE_ID, "NETCONF", null, null, null, writer);
            String response = out.toString();
            assertTrue("Response should contain ok", response.contains("ok"));
            resetOutBuffer();

            transportManagerStub.addAvcNotificationToQueue();
            METRICS_HANDLER.notificationReceived(sessionId);
            boolean notificationArrived = startThreadAndWaitForNotification(notificationTransformer, 5);
            if (!notificationArrived) {
                LOG.error("Failed to read notification in 5 secs for test {}", testName.getMethodName());
                fail();
            }

            String notification = removeEndTag(out.toString().trim());
            Path path = Paths.get(ClassLoader.getSystemResource("notification" + File.separator + "NetconfConfigChangeAvc.xml").toURI());
            final String expectedResult = new String(Files.readAllBytes(path));

            assertEquals(removeWhiteSpace(expectedResult), removeWhiteSpace(notification));
        } catch (final Exception exception) {
            LOG.error(LOG_ERROR_MSG, testName.getMethodName(), exception.getMessage(), exception);
            fail();
        }
    }

    private boolean startThreadAndWaitForNotification(final Callable<Boolean> notificationTransformer, final int waitTimeInSecs)
            throws InterruptedException {
        boolean result = true;
        adapterListener.setSubscribedState(true);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(notificationTransformer);

        int count = 0;
        while (transportManagerStub.getNotificationQueueSize() > 0) {
            Thread.sleep(1000);
            if (++count > waitTimeInSecs) {
                result = false;
                break;
            }
        }
        executorService.shutdownNow();
        return result;
    }

    private String removeWhiteSpace(final String output) {
        return output.replaceAll(">[\t|\r|\n| ]*<", "><").trim();
    }

    private String removeEndTag(final String output) {
        return output.substring(0, output.length() - VERSION110_END_TAG.length());
    }

    private void resetOutBuffer() {
        out.getBuffer().setLength(0);
    }

    @After
    public void tearDown() {
        try {
            writer.close();
            if (out != null) {
                out.close();
            }
        } catch (final Exception e) {
            LOG.error("Exception occurred when closing resource: {} ", e.getMessage(), e);
        }
    }

}
