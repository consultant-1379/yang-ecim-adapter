/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server.util;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;

public class NetconfSessionsUtil {

    private static NetconfSessionsUtil instance;

    private static final Logger logger = LoggerFactory.getLogger(NetconfSessionsUtil.class);
    private static TestProperties props = new TestProperties();

    private static NetconfSession adapterSession;
    private static NetconfSession nodeSession;

    private NetconfSessionsUtil() throws IOException {
        adapterSession = new NetconfSession(ADAPTER, props.valueOf(ADAPTER_HOST),               // NOSONAR
                props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
        nodeSession = new NetconfSession(props.valueOf(NODE_TYPE), props.valueOf(NODE_HOST),    // NOSONAR
                    props.valueOfInt(NODE_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
    }

    public static synchronized NetconfSessionsUtil getInstance() throws IOException {
        if(instance == null){
            logger.debug("Creating instance of NetconfSessionManager.");
            instance = new NetconfSessionsUtil();
        }
        return instance;
    }

    public void openSessions() throws IOException {
            adapterSession.open();
            nodeSession.open();
    }

    public void closeSessions() {
        if(adapterSession != null){
            adapterSession.close();
        }
        if(nodeSession != null){
            nodeSession.close();
        }
    }

    public void teardownSessions() {
        if(adapterSession != null){
            adapterSession.closeAll();
        }
        if(nodeSession != null){
            nodeSession.closeAll();
        }
    }

    public NetconfSession getNodeSession(){
        return nodeSession;
    }

    public NetconfSession getAdapterSession(){
        return adapterSession;
    }

}
