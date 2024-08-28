/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.NetconfServerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation.Halt;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation.Nop;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultNetconfHandler extends DefaultHandler2 {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNetconfHandler.class);

    private Operation operation;
    private final PrintWriter out;
    private int level;
    private final AtomicBoolean closed;

    CommandListener commandListener;

    public DefaultNetconfHandler(final CommandListener commandListener, final PrintWriter out,
            final AtomicBoolean closed) {
        this.commandListener = commandListener;
        this.out = out;
        this.level = 0;
        this.operation = new Nop();
        this.closed = closed;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        switch (this.level) {
        case 0:
            if (closed.get()) {
                throw new SAXException("Netconf session was closed. I won't process xml anymore");
            }
            this.operation = this.operation.processTag(localName, attributes, level);
            break;
        case Integer.MAX_VALUE:
            throw new SAXException("Maximum level of nesting is reached. Level " + this.level + ". Tag " + localName);
        default:
            this.operation = this.operation.processTag(localName, attributes, level);
            break;
        }
        this.level += 1;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        super.characters(ch, start, length);
        this.operation = this.operation.characters(ch, start, length);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        this.level -= 1;
        this.operation = this.operation.processEndTag(localName, level);
        if(level == 0){
            try{
                this.operation = this.operation.invoke(commandListener, out);
            } catch (NetconfServerException exception) {
                LOG.error(exception.getMessage(), exception);
                this.operation = new Halt();
            }
        }
    }

    public boolean isNop() {
        return this.operation instanceof Nop;
    }

    public boolean isHalt() {
        return this.operation instanceof Halt;
    }

    /**
     * Needed to reset the level when error occurred during SAX parse otherwise the next request will fail
     * 
     * @author ebialan
     */
    public void reset() {
        this.level = 0;
        this.operation = new Nop();
    }

    /**
     * Error handling Send error to client and reset Netconfhadnler status
     * 
     * @author ebialan
     */
    public void errorHandler(final String errorMessage) {
        if (!(this.operation instanceof Nop)) {
            this.operation = this.operation.sendError(commandListener, errorMessage, out);
        }
        reset();
    }
}
