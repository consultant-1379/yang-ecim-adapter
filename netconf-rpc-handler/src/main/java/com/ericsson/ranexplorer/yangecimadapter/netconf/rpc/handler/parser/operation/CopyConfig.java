/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class CopyConfig extends Rpc {

    private static final Logger logger = LoggerFactory.getLogger(CopyConfig.class);
    boolean copyConfig;
    boolean isSource;
    boolean isTarget;
    Datastore sourceDataStore;
    Datastore targetDataStore;

    protected CopyConfig(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        logger.info("level is {} ,tag is {} ", level, tag);
        switch (level) {
            case 2:
                switch (tag.toLowerCase()) {
                    case "source":
                        isSource = true;
                        break;
                    case "target":
                        isTarget = true;
                        break;
                }
                break;

            case 3:
                try {
                    if (level == 3 && isSource) {
                        sourceDataStore = Datastore.valueOf(tag.toUpperCase());
                    } else if (level == 3 && isTarget) {
                        targetDataStore = Datastore.valueOf(tag.toUpperCase());
                    }
                } catch (final IllegalArgumentException e) {
                    logger.warn("Unknown source for datastore: {}", tag);
                }
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        switch (level) {
            case 2:
                switch (tag.toLowerCase()) {
                    case "source":
                        isSource = false;
                        break;
                    case "target":
                        isTarget = false;
                        break;
                }
                break;
        }
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.copyConfig(messageId, sourceDataStore.toString(), targetDataStore.toString(), out);
        return new Nop();
    }

    @Override
    public Operation sendError(final CommandListener commandListener, final String errorMessage, final PrintWriter out) {
        return this;
    }

    @Override
    public String toString() {
        return "CopyConfig";
    }

}
