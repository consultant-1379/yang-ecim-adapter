/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MessageState;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MetricsHandler;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;

public class NetconfServerParser implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(NetconfServerParser.class);
    private static final MetricsHandler METRICS_HANDLER = MetricsHandler.INSTANCE;
    private final InputStream in;
    private final AtomicBoolean closed;
    private final CommandListener listener;
    private XMLReader parser;
    private final PrintWriter pw;
    private final int sessionId;

    public NetconfServerParser(final XMLReader parser, final InputStream in, final AtomicBoolean closed,
                               final int sessionId, final CommandListener listener, PrintWriter pw) {
        this.in = in;
        this.closed = closed;
        this.listener = listener;
        this.parser = parser;
        this.pw = pw;
        this.sessionId = sessionId;
    }

    public void parse(final PrintWriter pw) {
        final DefaultNetconfHandler xmlHandler = new DefaultNetconfHandler(listener, pw, this.closed);
        parser.setContentHandler(xmlHandler);
        parser.setErrorHandler(xmlHandler);
        try {
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (final SAXNotSupportedException sex) {
            logger.error("SAXNotSupportedException", sex);
        } catch (final SAXNotRecognizedException sex) {
            logger.error("SAXNotRecognizedException", sex);
        }
        String data;
        while (!closed.get()) {
            final java.util.Scanner s = new java.util.Scanner(in).useDelimiter("]]>]]>|\n##\n");
            if (!s.hasNext()) {
                break;
            }

            while (true) {
                if(closed.get() || !s.hasNext()){
                    break;
                }

                data = s.next();
                METRICS_HANDLER.markStart(sessionId, MessageState.MESSAGE_REQUEST);
                //remove framing tags for now, until better solution
                data = data.replaceAll("\n?#[0-9]+\n", "");
                final int indexOfStart = data.indexOf("<?");
                data = indexOfStart != -1 ? data.substring(indexOfStart) : data;
                try(StringReader stringReader = new StringReader(data)){
                    InputSource inputSource = new InputSource(stringReader);
                    logger.debug("parsing received data: [{}]", data);
                    parser.parse(inputSource);
                } catch (final IOException e) {
                    logger.error("IOException parse", e);
                } catch (final SAXException e) {
                    if (xmlHandler.isHalt()) {
                        closed.set(true);
                        throw new NetconfServerException();
                    } else if (!xmlHandler.isNop()) {
                        xmlHandler.errorHandler(e.getMessage());
                        logger.error("SAXException parse", e);
                    }
                } finally {
                    try{
                        METRICS_HANDLER.markEnd(sessionId, MessageState.MESSAGE_RESPONSE);
                    }
                    catch(Exception e){
                        logger.error("Error occurs in metrics handler", e);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        parse(pw);
    }
}
