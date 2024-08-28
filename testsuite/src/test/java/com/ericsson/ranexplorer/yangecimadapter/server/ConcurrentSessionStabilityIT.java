/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.NetconfSshSessionFailure;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSession;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.RPC_WRAPPER;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getValue;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper.getXmlWithoutWhitespace;
import static org.junit.Assert.assertTrue;

public class ConcurrentSessionStabilityIT {

    private static final long TEST_RUN_TIMEOUT = TimeUnit.MINUTES.toSeconds(25);

    // could also use file netconf/yangFilters/in/yang-rpc-get-with-ericsson-lrat-enb-adapter-filter.xml
    private static final String GET_LOADBALANCING_YANG = "netconf/yangFilters/in/yang-rpc-get-with-filter-loadbalancing.xml";
    private static final String GET_QCI_SUB_QUANTA_NODE = "netconf/yangFilters/in/yang-rpc-get-with-filter-qci-profile-predefined.xml";
    private static final String EDIT_QCI_SUB_QUANTA = "netconf/editConfigRealNode/editQciSubscriptionQuanta.xml";
    private static final String QCI_SUB_QUANTA = "qci-subscription-quanta";

    private TestProperties props = new TestProperties();

    @Test
    public void concurrentSessionTest() throws InterruptedException {
        List<Runnable> tests = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            tests.add(new GetConfig());
        }
        for(int i = 0; i < 10; i++){
            tests.add(new EditConfigSingleAtt(i));
        }
        Collections.shuffle(tests);
        assertConcurrent(tests);
    }

    //taken from junit-team wiki site: https://github.com/junit-team/junit4/wiki/Multithreaded-code-and-concurrency
    private static void assertConcurrent(final List<? extends Runnable> runnables) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(() -> {
                    allExecutorThreadsReady.countDown();
                    try {
                        afterInitBlocker.await();
                        submittedTestRunnable.run();
                    } catch (final Throwable e) {//NOSONAR
                        exceptions.add(e);
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            // wait until all threads are ready
            assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10L, TimeUnit.MILLISECONDS));
            // start all test runners
            afterInitBlocker.countDown();
            assertTrue("Concurrent Session test timeout! More than" + TEST_RUN_TIMEOUT + "seconds", allDone.await(TEST_RUN_TIMEOUT, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue("Concurrent Session test failed with exception(s)" + exceptions, exceptions.isEmpty());
    }

    class GetConfig implements Runnable{

        @Override
        public void run() {
            NetconfSession adapterNetconfSession = null;

            try {
                adapterNetconfSession = new NetconfSession("Adapter", props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterNetconfSession.open();

                String result = adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, "0", getXmlFromFile(GET_LOADBALANCING_YANG)));
                Pattern pattern = Pattern.compile(getXmlWithoutWhitespace("<rpc-reply message-id=\"[0-9]+\" " +
                        "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1\\.0\"><data>.*<enodeb-function " +
                        "xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-lrat-enb-adapter\">.*<\\/enodeb-function>" +
                        "<\\/data><\\/rpc-reply>"));
                Matcher matcher = pattern.matcher(getXmlWithoutWhitespace(result));
                if(!matcher.matches()){
                    throw new NetconfSshSessionFailure("The get request returned unexpected result!");
                }


            } catch (IOException e) {
                throw new NetconfSshSessionFailure("Error occurred during get request", e);
            } finally {
                if (adapterNetconfSession != null) {
                    adapterNetconfSession.closeAll();
                }
            }
        }
    }

    class EditConfigSingleAtt implements Runnable{

        private String identifier;

        private EditConfigSingleAtt(int id) {
            identifier = (id == 0) ? "default" : "qci" + Integer.toString(id);
        }

        @Override
        public void run() {
            try {
                NetconfSession adapterNetconfSession = new NetconfSession("Adapter", props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterNetconfSession.open();

                String responseQci = adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, identifier, getXmlFromFile(GET_QCI_SUB_QUANTA_NODE, identifier)));
                int origQciSubQuanta = Integer.parseInt(getValue(responseQci, QCI_SUB_QUANTA));
                int newQciSubQuanta = origQciSubQuanta + 1;

                String response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, identifier, Integer.toString(newQciSubQuanta)));
                if(!response.contains("<ok/>")){
                    throw new NetconfSshSessionFailure("The edit request returned unexpected result!");
                }
                response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile(EDIT_QCI_SUB_QUANTA, identifier, Integer.toString(origQciSubQuanta)));
                if(!response.contains("<ok/>")){
                    throw new NetconfSshSessionFailure("The edit request returned unexpected result!");
                }
            } catch (IOException e) {
                throw new NetconfSshSessionFailure("Error occurred during get request", e);
            }
        }
    }
}
