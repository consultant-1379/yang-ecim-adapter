/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.NETSIM;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.*;
import static org.junit.Assert.*;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSessionsUtil;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSession;

import java.io.IOException;

public class AdapterServerIT {

    private static final Logger logger = LoggerFactory.getLogger(AdapterServerIT.class);
    private static final String KILL_SESSION_FILE = "netconf/killSession.xml";
    private static final String EXCEPTION_MESSAGE = "Exception occurred  in kill session test {}!";

    private static final TestProperties props = new TestProperties();

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

    @After
    public void closeSessions(){
        nsu.closeSessions();
    }

    @Test
    public void testCloseSession_sendNetconfCloseSessionOverSSH_sessionClosed() { // NOSONAR
        try {
            nsu.getAdapterSession().open();
            assertTrue(nsu.getAdapterSession().channelIsOpen());

            String response = nsu.getAdapterSession().sendMessage(getXmlFromFile("netconf/closeSession.xml"));
            assertTrue(response.contains("<ok/>"));
            assertTrue(nsu.getAdapterSession().waitForChannelToClose());
            assertTrue(nsu.getAdapterSession().channelIsClosed());
        } catch (Exception exception) {
            logger.error("Exception occurred in close session test!", exception);
            fail();
        }
    }

    @Test
    public void testKillSessionOverSSH_startTwoSessionsAndKillFirstSessionUsingSecondSession_FirstSessionIsClosed() { // NOSONAR
        NetconfSession secondAdapterSession = null;
        try {
            String response = nsu.getAdapterSession().open();
            String sessionId = getValue(response, "session-id");

            assertTrue(nsu.getAdapterSession().channelIsOpen());

            secondAdapterSession = new NetconfSession("SecondAdapterSession", props.valueOf(ADAPTER_HOST),
                    props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
            secondAdapterSession.open();

            response = secondAdapterSession.sendMessage(getXmlFromFile(KILL_SESSION_FILE, sessionId));

            if (NETSIM.equals(props.valueOf(TestProperty.NODE_TYPE))) {
                //netsim doesn't support kill-session command
                assertTrue(response.contains("Unexpected error: Unknown command: kill-session"));
                assertTrue(nsu.getAdapterSession().channelIsOpen());
                nsu.getAdapterSession().sendMessage(getXmlFromFile("netconf/closeSession.xml"));
            }else {
                assertTrue(response.contains("<ok/>"));
            }

            assertTrue(nsu.getAdapterSession().waitForChannelToClose());
            assertTrue(nsu.getAdapterSession().channelIsClosed());
            assertTrue(secondAdapterSession.channelIsOpen());

        } catch (Exception exception) {
            logger.error(EXCEPTION_MESSAGE, testName.getMethodName(), exception);
            fail();
        } finally {
            if(secondAdapterSession != null) {
                secondAdapterSession.close();
            }
        }
    }

    @Test
    public void testKillSessionOverSSHWithWrongId() {
        try {
            nsu.getAdapterSession().open();
            assertTrue(nsu.getAdapterSession().channelIsOpen());

            String response = nsu.getAdapterSession().sendMessage(getXmlFromFile(KILL_SESSION_FILE, "1002"));
            assertTrue(response.contains("<rpc-error"));
        } catch (Exception exception) {
            logger.error(EXCEPTION_MESSAGE, testName.getMethodName(), exception);
            fail();
        }
    }

    @Test
    public void testKillSessionOverSSHWithTheSameId() {
        try {
            String response = nsu.getAdapterSession().open();
            String sessionId = getValue(response, "session-id");
            assertTrue(nsu.getAdapterSession().channelIsOpen());

            response = nsu.getAdapterSession().sendMessage(getXmlFromFile(KILL_SESSION_FILE, sessionId));
            assertTrue(response.contains("<rpc-error"));
        } catch (Exception exception) {
            logger.error(EXCEPTION_MESSAGE, testName.getMethodName(), exception);
            fail();
        }
    }
}
