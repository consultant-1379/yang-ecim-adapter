/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.RPC_WRAPPER;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
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

public class GetPerformanceTimingIT {
    private static final Logger LOG = LoggerFactory.getLogger(GetPerformanceTimingIT.class);

    private static final String SESSION_OPENS = "Open session";
    private static final String SESSION_CLOSES = "Close session";
    private static final String RESPONSE_TIME = "Response time";
    private static final String EXCEPTION_OCCURRED = "Exception occurred while executing {} : {}";
    private static final String LOG_STRING = "{} - {}: {}ms";
    private static final int QCI_PROFILE_PREDEFINED_ID = 9;

    private NetconfSession adapterNetconfSession;
    private static Timer timer = new Timer();
    private String methodName;

    private TestProperties props = new TestProperties();

    @Rule
    public final TestName testName = new TestName();

    @Test
    public void testGet_SingleAttribute_UsingFilter(){ //NOSONAR
        final String qciQuantaAdapter = "netconf/yangFilters/in/getSingleAttributeUsingYangFilter.xml";
        methodName = testName.getMethodName();
        timer.start();
        try {
            openNetconfSession();
            LOG.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());

            final long startResponse = timer.now();
            adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, "2", getXmlFromFile(qciQuantaAdapter, Integer.toString(QCI_PROFILE_PREDEFINED_ID))));
            LOG.debug(LOG_STRING, methodName, RESPONSE_TIME, timer.getDuration(startResponse));

            final long startCloseSession = timer.now();
            closeNetconfSession(adapterNetconfSession);
            LOG.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(startCloseSession));
        } catch(Exception exception){
            LOG.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        } finally {
            LOG.debug("TestGet_SingleAttr ends: {}ms", timer.getElapsedTime());
        }
    }

    @Test
    public void testGet_SingleMO_UsingFilter(){ //NOSONAR
        final String cellRelationMoAdapter = "netconf/yangFilters/in/getMOUsingYangFilter.xml";
        methodName = testName.getMethodName();
        timer.start();
        try{
            openNetconfSession();
            LOG.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());

            final String [] data = {"LTE01dg2ERBS00001-1", "1", "314"};
            final long startResponse = timer.now();
            adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(cellRelationMoAdapter, data)));
            LOG.debug(LOG_STRING, methodName, RESPONSE_TIME, timer.getDuration(startResponse));

            final long startCloseSession = timer.now();
            closeNetconfSession(adapterNetconfSession);
            LOG.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(startCloseSession));
        } catch(Exception exception){
            LOG.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        } finally {
            LOG.debug("TestGet_SingleMO ends: {}ms", timer.getElapsedTime());
        }
    }

    @Test
    public void testGet_AllMOs_WithoutFilter(){ //NOSONAR
        final String getAll = "netconf/getAllWithoutFilter.xml";
        methodName = testName.getMethodName();
        timer.start();
        try {
            openNetconfSession();
            LOG.debug(LOG_STRING, methodName, SESSION_OPENS, timer.getElapsedTime());

            final long startResponse = timer.now();
            adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(getAll)));
            LOG.debug(LOG_STRING, methodName, RESPONSE_TIME, timer.getDuration(startResponse));

            final long startCloseSession = timer.now();
            closeNetconfSession(adapterNetconfSession);
            LOG.debug(LOG_STRING, methodName, SESSION_CLOSES, timer.getDuration(startCloseSession));
        } catch (Exception exception) {
            LOG.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        }
        finally{
            LOG.info("testGetAllMOs ends: {} ms", timer.getElapsedTime());
        }
    }

    private void openNetconfSession(){
        try {
            adapterNetconfSession = new NetconfSession(methodName + " - adapterNetconfSession",
                    props.valueOf(ADAPTER_HOST), props.valueOfInt(ADAPTER_PORT),
                    props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
            adapterNetconfSession.open();
        } catch (IOException exception) {
            LOG.error(EXCEPTION_OCCURRED, methodName, exception.getMessage(), exception);
            fail();
        }
    }

    private void closeNetconfSession(final NetconfSession netconfSession){
        if(netconfSession != null){
            netconfSession.close();
        }
    }
}
