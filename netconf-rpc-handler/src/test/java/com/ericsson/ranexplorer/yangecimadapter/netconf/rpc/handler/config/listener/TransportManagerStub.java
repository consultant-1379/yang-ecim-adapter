/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ericsson.oss.mediation.transport.api.TransportData;
import com.ericsson.oss.mediation.transport.api.TransportManager;
import com.ericsson.oss.mediation.transport.api.exception.TransportException;
import com.ericsson.oss.mediation.transport.api.provider.TransportProvider;
import com.ericsson.ranexplorer.yangecimadapter.netconf.client.NotificationQueueProvider;

public class TransportManagerStub implements TransportManager, NotificationQueueProvider {

    private Queue<String> notificationQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void openConnection() throws TransportException {
        // Not used
    }

    @Override
    public void closeConnection() throws TransportException {
        // Not used
    }

    @Override
    public void sendData(TransportData request) throws TransportException {
        // Not used
    }

    @Override
    public void readData(TransportData response) throws TransportException {
        // Not used
    }

    @Override
    public void readData(TransportData response, char[] filterCharacters) throws TransportException {
        // Not used
    }

    @Override
    public InputStream getTransportInputStream() {
        return null;
    }

    @Override
    public TransportProvider getTransportProvider() {
        return null;
    }

    @Override
    public Queue<String> getNotificationQueue() {
        return notificationQueue;
    }

    public void addAvcNotificationToQueue() throws IOException, URISyntaxException {
        final String fileName = "notification" + File.separator + "Avc.xml";
        Path path = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
        final String avc = new String(Files.readAllBytes(path));
        notificationQueue.offer(avc);
    }

    public int getNotificationQueueSize() {
        return notificationQueue.size();
    }

}
