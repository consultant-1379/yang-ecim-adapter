/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.util;

import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ONE_DOT_ONE_TERMINATION;
import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.ONE_DOT_ZERO_TERMINATION;
import static com.ericsson.ranexplorer.yangecimadapter.server.constants.IntTestConstants.VERIFY_TIMEOUT;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.FileReader.getXmlFromFile;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.StringUtils.removeInitialAndTerminationStringFrom;
import static com.ericsson.ranexplorer.yangecimadapter.server.util.NetconfVersion.NETCONF_1_1;

import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.MessageFailedException;
import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.NetconfSshSessionFailure;
import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.TimedOutException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NetconfSession {

    private static final Logger logger = LoggerFactory.getLogger(NetconfSession.class);

    private static final long REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

    private String name;
    private String host;
    private int port;
    private String user;
    private String password;
    private NetconfVersion version;
    private SshClient client;
    private ClientSession sshSession;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream err;
    private PipedOutputStream pipedIn;
    private PipedInputStream in;
    private ClientChannel channel;
    private boolean sessionOpen;

    private NetconfSession() {

    }

    public NetconfSession(final String name, final String host, final int port, final String user, final String password) throws IOException {
        this.name = name;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        createAndStartClient();
    }

    private void createAndStartClient() {
        client = SshClient.setUpDefaultClient();
        client.start();
    }

    private void createSshSession(final String host, final int port, final String user, final String password) throws IOException {
        logger.debug("creating session host:{} port:{} user:{} pass:{}", host, port, user, password);
        sshSession = client.connect(user, host, port).verify(VERIFY_TIMEOUT, TimeUnit.SECONDS).getSession();
        sshSession.addPasswordIdentity(password);
        sshSession.auth().verify(VERIFY_TIMEOUT, TimeUnit.SECONDS);
        sshSession.waitFor(EnumSet.of(ClientSession.ClientSessionEvent.AUTHED), VERIFY_TIMEOUT);
    }

    private void createSshSession() throws IOException {
        createSshSession(host, port, user, password);
    }

    public String getName() {
        return name;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public String open() throws IOException {
        createSshSession();

        if (version == NETCONF_1_1) {
            return openNetconf11Session();
        } else {
            return openNetconf10Session();
        }
    }

    private String openNetconf11Session() {
        return openUsingHelloFromFileWithName("netconf/hello-v1.1.xml");
    }

    private String openNetconf10Session() {
        return openUsingHelloFromFileWithName("netconf/hello-v1.0.xml");
    }

    private String openUsingHelloFromFileWithName(final String fileName) {
        String response;
        try {
            outputStream = new ByteArrayOutputStream();
            err = new ByteArrayOutputStream();
            pipedIn = new PipedOutputStream();
            in = new PipedInputStream(pipedIn);
            channel = sshSession.createSubsystemChannel("netconf");

            channel.setOut(outputStream);
            channel.setErr(err);
            channel.setIn(in);
            channel.open();
            response = sendMessage(getXmlFromFile(fileName));
            sessionOpen = true;
        } catch (Exception exception) {
            throw new NetconfSshSessionFailure("Failed to open netconf ssh session", exception);
        }
        return response;
    }

    public String sendMessage(final String message) {
        return removeInitialAndTerminationStringFrom(sendMessageAndGetRawResponse(message));
    }

    public String sendMessageAndGetRawResponse(final String message) {
        logger.info("Requested to send message on netconf session '{}'\n{}", name, message);
        byte[] bytesToSend = getBytesToSendFromString(message);
        String response;
        try {
            pipedIn.write(bytesToSend);
            pipedIn.flush();
            response = waitForRawResponse();
            logger.info("Received the following response from netconf session {}\n{}", name, response);
        } catch (final IOException | InterruptedException exception) {
            logger.error("Exception thrown on netconf session {} while trying to send message", name, exception);
            throw new MessageFailedException(String.format("Failed to send message on netconf session %s", name), exception);
        }
        return response;
    }

    private byte[] getBytesToSendFromString(String message) {
        return getBytesFromString(getStringToSend(message));
    }

    private byte[] getBytesFromString(String string) {
        return string.getBytes(Charset.defaultCharset());
    }

    private String getStringToSend(String message) {
        if (version == NETCONF_1_1) {
            return "#" + message.length() + "\n" + message + ONE_DOT_ONE_TERMINATION;
        } else {
            return message + ONE_DOT_ZERO_TERMINATION;
        }
    }

    private Boolean isOutputStreamTerminated() {
        // using trim() because extra line breaks are added to end of responses
        return outputStream.toString().trim().endsWith(ONE_DOT_ONE_TERMINATION.trim())
                || outputStream.toString().trim().endsWith(ONE_DOT_ZERO_TERMINATION);
    }

    private String waitForRawResponse() throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        while (!isOutputStreamTerminated()) {
            Thread.sleep(100);

            if (System.currentTimeMillis() > (startTime + REQUEST_TIMEOUT)) {
                throw new TimedOutException(String.format(
                        "Timeout of %s ms exceeded while waiting for response from outputStream for netconf session %s", REQUEST_TIMEOUT, name));
            }
        }
        final String response = outputStream.toString();
        outputStream.reset();
        return response;
    }



    public boolean waitForChannelToClose() {
        Set<ClientChannelEvent> channelEvents = channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), REQUEST_TIMEOUT);
        return channelEvents.contains(ClientChannelEvent.CLOSED);
    }

    public boolean channelIsOpen() {
        return channel.isOpen();
    }

    public boolean channelIsClosed() {
        return !channel.isOpen();
    }

    public String close() {
        String response = closeNetconfSession();
        logger.info("Closing ssh channel and streams for netconf session {}", name);
        closeChannel();
        closePipedInputStream();
        closeOutputStream(pipedIn, "pipedIn");
        closeOutputStream(err, "err");
        closeOutputStream(outputStream, "outputStream");
        return response;
    }

    public String closeAll() {
        String response = close();
        logger.info("Closing session and client for netconf session {}", name);
        closeSshSession();
        closeClient();
        return response;
    }

    private String closeNetconfSession() {
        String response = null;
        try {
            if (sessionOpen && channelIsOpen()) {
                logger.info("Sending close session rpc message for channel {}", name);
                response = sendMessage(getXmlFromFile("netconf/closeSession.xml"));
                sessionOpen = false;
            } else {
                logger.warn("No session ongoing for netconf session {}", name);
            }
        } catch (Exception exception) {
            logger.error("Failed to close the netconf session {}", name, exception);
        }
        return response;

    }

    private void closeChannel() {
        try {
            if (channel != null && channelIsOpen()) {
                channel.close();
            }
        } catch (final IOException exception) {
            logger.error("Failed to close the channel for netconf session {}", name, exception);
        }
    }

    private void closeOutputStream(final OutputStream outStream, final String varName) {
        try {
            if (outStream != null) {
                outStream.close();
            }
        } catch (final IOException exception) {
            logger.error("Failed to close the outputStream with variable name {} for netconf session {}", varName, name, exception);
        }
    }

    private void closePipedInputStream() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (final IOException exception) {
            logger.error("Failed to close the PipedInputStream in for netconf session {}", name, exception);
        }
    }

    private void closeSshSession() {
        if (sshSession != null) {
            try {
                sshSession.close();
            } catch (final IOException exception) {
                logger.error("Failed to close the sshSession for netconf session {}", name, exception);
            }
        }
    }

    private void closeClient() {
        if (client != null) {
            client.stop();
        }
    }

    public void setVersion(NetconfVersion version) {
        this.version = version;
    }

    public NetconfVersion getVersion() {
        return version;
    }

    public static final class Builder {
        final NetconfSession session;

        public Builder() {
            session = new NetconfSession();
        }

        public Builder name(String name) {
            session.name = name;
            return this;
        }

        public Builder host(String host) {
            session.host = host;
            return this;
        }

        public Builder port(int port) {
            session.port = port;
            return this;
        }

        public Builder user(String user) {
            session.user = user;
            return this;
        }

        public Builder password(String password) {
            session.password = password;
            return this;
        }

        public Builder version(NetconfVersion version) {
            session.version = version;
            return this;
        }

        public NetconfSession build() throws IOException {
            if (areSuppliedValuesValid()) {
                createSshClientAndSession();
                return session;
            } else throw new IllegalStateException(getExceptionMessage());
        }

        private boolean areSuppliedValuesValid() {
            setNameIfItIsEmpty();
            return isHostValid() && isPortValid() && isUserValid() && isPasswordValid();
        }

        private void setNameIfItIsEmpty() {
            if (isNameInvalid()) {
                session.name = UUID.randomUUID().toString();
            }
        }

        private boolean isNameInvalid() {
            return session.name == null || session.name.isEmpty();
        }

        private boolean isHostValid() {
            return session.host != null && !session.host.isEmpty();
        }

        private boolean isPortValid() {
            return session.port != 0;
        }

        private boolean isUserValid() {
            return session.user != null && !session.user.isEmpty();
        }

        private boolean isPasswordValid() {
            return session.password != null && !session.password.isEmpty();
        }

        private void createSshClientAndSession() throws IOException {
            session.createAndStartClient();
            session.createSshSession();
        }

        private String getExceptionMessage() {
            if (session.password == null || session.password.isEmpty()) {
                return "Invalid connection details supplied. Host: " + session.host + ", Port: " +
                        session.port + ", User: " + session.user + ", Password: " + session
                        .password;
            } else return "Invalid connection details supplied. Host: " + session.host + ", Port: "
                    + session.port + ", User: " + session.user;
        }
    }
}
