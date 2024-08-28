/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;


import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSession;
import com.ericsson.ranexplorer.yangecimadapter.common.services.util.Timer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EditConfigPerformanceTimingIT {

    private static final Logger logger = LoggerFactory.getLogger(EditConfigPerformanceTimingIT.class);

    private static final String EDIT_QCI_SUB_QUANTA = "netconf/editConfigPerformanceTiming/editQciSubscriptionQuanta.xml";
    private static final String EDIT_SINGLE_MO =
            "netconf/editConfigPerformanceTiming/editConfigSingleMO/editConfigSingleMO.xml";
    private static final String RESET_SINGLE_MO =
            "netconf/editConfigPerformanceTiming/editConfigSingleMO/resetSingleMO.xml";
    private static final String EDITCONFIG_MULTIPLE_MOS =
            "netconf/editConfigPerformanceTiming/editConfigMultipleMOs/editConfigMultipleMO.xml";
    private static final String RESET_EDITCONFIG_MULTIPLE_MOS =
            "netconf/editConfigPerformanceTiming/editConfigMultipleMOs/resetMultipleMOs.xml";
    private static final String OK_XML_TAG = "<ok/>";

    private static final String SESSION_OPENS = "Open session";
    private static final String SESSION_CLOSES = "Close session";
    private static final String EDIT_RESPONSE = "editResponse ends";
    private static final String RESET_RESPONSE = "resetResponse ends";
    private static final String EXCEPTION_OCCURRED = "Exception occurred while executing {} : {}";
    private static final String LOG_STRING = "{} - {} : {}ms";

    private long timeStart;
    private long timeToRemove;
    private String methodName="";

    private NetconfSession adapterNetconfSession;
    private Timer timer = new Timer();

    private TestProperties props = new TestProperties();

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void testEditConfig_updateSingleAttribute(){ //NOSONAR
        methodName = testName.getMethodName();
        try {
            timer.start();
            openNetconfSession();
            logger.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());

            // Set the new value for qciSubscriptionQuanta using the adapter
            timeStart = timer.now();
            String editResponse = adapterNetconfSession.sendMessage(getXmlFromFile(EDIT_QCI_SUB_QUANTA, "11"));
            logger.debug(LOG_STRING, methodName, EDIT_RESPONSE, timer.getDuration(timeStart));
            assertTrue(editResponse.contains(OK_XML_TAG));


            // Reset value back to original
            timeStart = timer.now();
            String resetResposne = adapterNetconfSession.sendMessage(getXmlFromFile(EDIT_QCI_SUB_QUANTA, "1"));
            timeToRemove = timer.getDuration(timeStart);
            logger.debug(LOG_STRING, methodName, RESET_RESPONSE, timeToRemove);
            assertTrue(resetResposne.contains(OK_XML_TAG));
        } catch (final Exception exception) {
            logger.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        } finally {
            timeStart = timer.now();
            closeNetconfSession(adapterNetconfSession);
            logger.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(timeStart));
            logger.debug("Ends EditConfig  SingleAttr: {}ms", timer.getElapsedTime()-timeToRemove);
        }
    }

    @Test
    public void testEditConfig_SingleMO() { //NOSONAR
        methodName = testName.getMethodName();
        try {
            timer.start();
            openNetconfSession();
            logger.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());


            // Set new values of the attributes
            timeStart = timer.now();
            String editResponse = adapterNetconfSession.sendMessage(getXmlFromFile(EDIT_SINGLE_MO));
            logger.debug(LOG_STRING, methodName, EDIT_RESPONSE, timer.getDuration(timeStart));
            assertTrue(editResponse.contains(OK_XML_TAG));

            // Reset the values back to original
            timeStart = timer.now();
            String resetResponse = adapterNetconfSession.sendMessage(getXmlFromFile(RESET_SINGLE_MO));
            timeToRemove = timer.getDuration(timeStart);
            logger.debug(LOG_STRING, methodName, RESET_RESPONSE, timeToRemove);
            assertTrue(resetResponse.contains(OK_XML_TAG));

        } catch (final Exception exception) {
            logger.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        } finally {
            timeStart = timer.now();
            closeNetconfSession(adapterNetconfSession);
            logger.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(timeStart));
            logger.debug("Ends EditConfig SingleMO: {}ms", timer.getElapsedTime()-timeToRemove);
        }
    }

    @Test
    public void testEditConfig_MultipleMOsMultipleOperations() { //NOSONAR
        methodName = testName.getMethodName();
        try {
            timer.start();
            openNetconfSession();
            logger.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());

            timeStart = timer.now();
            // Set new values of the attributes
            String editResponse = adapterNetconfSession.sendMessage(getXmlFromFile(EDITCONFIG_MULTIPLE_MOS));
            logger.debug(LOG_STRING, methodName, EDIT_RESPONSE, timer.getDuration(timeStart));
            assertTrue(editResponse.contains(OK_XML_TAG));

            timeStart = timer.now();
            // Reset the values back to original
            String resetResponse = adapterNetconfSession.sendMessage(getXmlFromFile(RESET_EDITCONFIG_MULTIPLE_MOS));
            timeToRemove = timer.getDuration(timeStart);
            logger.debug(LOG_STRING, methodName, RESET_RESPONSE, timeToRemove);
            assertTrue(resetResponse.contains(OK_XML_TAG));
        } catch (final Exception exception) {
            logger.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        } finally {
            timeStart = timer.now();
            closeNetconfSession(adapterNetconfSession);
            logger.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(timeStart));
            logger.debug("Ends EditConfig MultipleMOs MultipleOperations: {}ms", timer.getElapsedTime()-timeToRemove);
        }
    }

    private void openNetconfSession(){
        try {
            adapterNetconfSession = new NetconfSession("Adapter", props.valueOf(ADAPTER_HOST),
                    props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
            adapterNetconfSession.open();
        } catch (IOException exception) {
            logger.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        }
    }

    private void closeNetconfSession(final NetconfSession netconfSession) {
        if (netconfSession != null) {
            netconfSession.close();
        }
    }
}

