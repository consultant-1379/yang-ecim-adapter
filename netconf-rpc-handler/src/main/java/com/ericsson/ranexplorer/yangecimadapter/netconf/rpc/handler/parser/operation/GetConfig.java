/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.io.PrintWriter;

public class GetConfig extends Rpc {

    private static final Logger logger = LoggerFactory.getLogger(GetConfig.class);

    private boolean isSource;
    private Datastore datastore;
    private Filter filter;
    private String filterType;
    private StringBuilder filterBody;

    protected GetConfig(final Rpc rpc) {
        super(rpc);
        this.isSource = false;
        this.datastore = Datastore.RUNNING;
    }

    @Override
    public GetConfig processTag(final String tag, final Attributes attributes, final int level) {
        switch (level) {
        case 2:
            switch (tag.toLowerCase()) {
            case "source":
                isSource = true;
                break;
            case "filter":
                filterType = attributes.getValue("type");
                filterBody = new StringBuilder();
                break;
            }
            break;
        default:
            if (level == 3 && isSource) {
                try {
                    datastore = Datastore.valueOf(tag.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown source for datastore: {}", tag);
                }
            } else if (level > 2 && filterBody != null) {
                appendTagAttributes(tag, attributes);
            }
            break;
        }
        return this;
    }

    private void appendTagAttributes(final String tag, final Attributes attributes) {
        filterBody.append("<").append(tag);
        for (int i = 0; i < attributes.getLength(); i++) {
            if (!attributes.getQName(i).contains("xmlns")) {
                filterBody.append(" ").append(attributes.getLocalName(i)).append("=\"")
                        .append(attributes.getValue(i)).append("\"");
            }
        }
        filterBody.append(">");
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        if (filterBody != null) {
            filterBody.append(ch, start, length);
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
            case "filter":
                filter = new Filter() {
                    private String type;
                    private String body;

                    Filter init(final String type, final StringBuilder body) {
                        this.type = type;
                        this.body = body.toString();
                        return this;
                    }

                    @Override
                    public String getType() {
                        return type;
                    }

                    @Override
                    public String asString() {
                        return body;
                    }
                }.init(filterType, filterBody);
                filterType = null;
                filterBody = null;
                break;
            }
        default:
            if (level > 2 && filterBody != null) {
                filterBody.append("</").append(tag).append(">");
            }
            break;
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.getConfig(messageId, datastore, filter, out);
        return new Nop();
    }
}
