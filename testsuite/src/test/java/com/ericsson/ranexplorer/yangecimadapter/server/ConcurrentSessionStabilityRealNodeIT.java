/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.server;

import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.NetconfSshSessionFailure;
import com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperties;
import com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfSession;
import com.ericsson.ranexplorer.yangecimadapter.server.util.XmlHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.properties.TestProperty.*;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConcurrentSessionStabilityRealNodeIT {
    private static final long TEST_RUN_TIMEOUT = TimeUnit.MINUTES.toSeconds(25);

    private TestProperties props = new TestProperties();

    @Ignore
    @Test
    public void concurrentSessionTestWithRepeatingIds() throws InterruptedException {
        final int[] qciId = {1,2,3,4,1,2,3,4,1,2};
        final int[] qciSubscriptionQuanta = {1,2,3,4,5,6,7,8,9,10};
        List<Runnable> tests = new ArrayList<>();
        for(int i = 0; i < qciId.length; i++){
            tests.add(new EditConfigSingleAtt(qciId[i], qciSubscriptionQuanta[i]));
        }
        assertConcurrent(tests);
    }

    @Ignore
    @Test
    public void concurrentSessionTestWithDifferentId() throws InterruptedException {
        final int[] qciId = {1,2,3,4,5,6,7,8,9};
        final int[] qciSubscriptionQuanta = {1,2,3,4,5,6,7,8,9};
        List<Runnable> tests = new ArrayList<>();
        for(int i = 0; i < qciId.length; i++){
            tests.add(new EditConfigSingleAtt(qciId[i], qciSubscriptionQuanta[i]));
        }
        assertConcurrent(tests);
    }

    @Test
    public void concurrentSessionTestWithSameId() throws InterruptedException {
        final int qciId = 1;
        final int[] qciSubscriptionQuanta = {1,2,3,4,5,6,7,8,9,10};
        List<Runnable> tests = new ArrayList<>();
        for(int i = 0; i < qciSubscriptionQuanta.length; i++){
            tests.add(new EditConfigSingleAtt(qciId, qciSubscriptionQuanta[i]));
        }
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

    class EditConfigSingleAtt implements Runnable {
        int qciId;
        int qciSubscriptionQuanta;
        public EditConfigSingleAtt(int id, int quanta){
            this.qciId = id;
            this.qciSubscriptionQuanta = quanta;
        }
        @Override
        public void run() {
            NetconfSession adapterNetconfSession = null;
            final String qciQuantaAdapter = "netconf/yangFilters/in/getSingleAttributeUsingYangFilter.xml";

            try {
                adapterNetconfSession = new NetconfSession("Adapter", props.valueOf(ADAPTER_HOST),
                        props.valueOfInt(ADAPTER_PORT), props.valueOf(SSH_USER), props.valueOf(SSH_PASS));
                adapterNetconfSession.open();
                String qciProfilePredefinedId="qci" + qciId;
                String response = adapterNetconfSession.sendMessageAndGetRawResponse(getXmlFromFile("netconf/editConfigRealNode/editQciSubscriptionQuanta.xml", qciProfilePredefinedId, Integer.toString(qciSubscriptionQuanta)));
                if (!response.contains("<ok/>")) {
                    throw new NetconfSshSessionFailure("The edit request returned unexpected result!");
                }
                else{
                    final String adapterResponse = adapterNetconfSession.sendMessage(String.format(RPC_WRAPPER, "2", getXmlFromFile(qciQuantaAdapter, qciProfilePredefinedId)));
                    final int actualValue = Integer.parseInt(XmlHelper.getValue(adapterResponse, "qci-subscription-quanta"));
                    assertEquals(qciSubscriptionQuanta, actualValue );
                }
            } catch (IOException e) {
                throw new NetconfSshSessionFailure("Error occurred during get request", e);
            }
            finally {
                if(adapterNetconfSession!=null) {
                    adapterNetconfSession.closeAll();
                }
            }
        }
    }
}
