package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.server;

import com.ericsson.oss.mediation.util.netconf.api.NetconfConnectionStatus;
import com.ericsson.oss.mediation.util.netconf.api.NetconfManager;
import com.ericsson.oss.mediation.util.netconf.api.NetconfResponse;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.filter.SubTreeFilter;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerConnectionException;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MessageState;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MetricsHandler;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.SshConfiguration;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.AdapterListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.ssh.SshNetconfSession;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class NetconfSessionTest {
    private Logger logger = LoggerFactory.getLogger(NetconfSessionTest.class);

    private static final String ECIM_GET_STRING = "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">" +
            "<managedElementId>LTE01dg2ERBS00001</managedElementId>" +
            " <ENodeBFunction xmlns=\"urn:com:ericsson:ecim:Lrat\">" +
            "  <eNodeBFunctionId>1</eNodeBFunctionId>" +
            "  <LoadBalancingFunction>" +
            "    <loadBalancingFunctionId>1</loadBalancingFunctionId>" +
            "    <lbThreshold>100</lbThreshold>" +
            "    <lbCeiling/>" +
            "  </LoadBalancingFunction>" +
            " </ENodeBFunction>" +
            "</ManagedElement>";
    private static final String YANG_GET_STRING  = "<rpc-reply message-id=\"0\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
            "<data>" +
            " <enodeb-function " +
            "  xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">   " +
            "   <load-balancing-function>      " +
            "    <lb-threshold>100</lb-threshold>" +
            "   </load-balancing-function>" +
            " </enodeb-function>" +
            "</data>" +
            "</rpc-reply>]]>]]>";

    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;

    @Mock
    private NetconfManager manager;

    @Mock
    private ServerSession serverSession;

    private NetconfSession netconfSession;
    private int sessionId = 100;

    @Before
    public void setUp() throws NetconfManagerException {
        MockitoAnnotations.initMocks(this);
        AdapterListener adapterListener = new AdapterListener();
        Whitebox.setInternalState(adapterListener, "netconfManager", manager);
        netconfSession = Mockito.spy(new SshNetconfSession((exitValue, exitMessage) -> {/*nothing here*/},
                new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(),
                adapterListener, new SshConfiguration(), "admin", serverSession));
        when(netconfSession.getNetconfManager()).thenReturn(manager);
        when(manager.getStatus()).thenReturn(NetconfConnectionStatus.NOT_CONNECTED);
        when(manager.getSessionId()).thenReturn("1");
        NetconfResponse netconfResponse = new NetconfResponse();
        netconfResponse.setError(false);
        netconfResponse.setData(ECIM_GET_STRING);
        when(manager.get(any(SubTreeFilter.class))).thenReturn(netconfResponse);
        adapterListener.setSessionId(sessionId);
        METRICS_HANDLER.markStartCreateSession(sessionId);
        METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_REQUEST);
    }

    @Test
    public void testReconnectOnGetSuccess() throws NetconfManagerException {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        when(manager.connect()).thenReturn(new NetconfResponse());
        netconfSession.get("0", null, writer);
        String result = out.toString().replaceAll("[\t|\r|\n|\\s+]", "");
        result = removeDataBetweenTags(result, "<netconf-state", "</netconf-state>");
        result = removeDataBetweenTags(result, "<netconf", "</netconf>");
        assertEquals(YANG_GET_STRING.replaceAll("[\t|\r|\n|\\s+]", ""), result);
        writer.close();
    }

    @Test(expected = NetconfServerConnectionException.class)
    public void testReconnectOnGetException() {
        try(StringWriter out = new StringWriter();
            PrintWriter writer = new PrintWriter(out)) {
            when(manager.connect()).thenThrow(new NetconfManagerException());
            netconfSession.get("0", null, writer);
        } catch (IOException | NetconfManagerException e) {
            logger.error("Error occured!", e);
            fail();
        }

    }

    private String removeDataBetweenTags(final String input, final String startTag, final String endTag) {
        String stringToRemove = input.substring(input.indexOf(startTag), input.indexOf(endTag) + endTag.length());
        return input.replace(stringToRemove, "");
    }

}
