/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.io.PrintWriter;

public class Get extends Rpc {

    private static final Logger logger = LoggerFactory.getLogger(Get.class);

    private Filter filter;
    private String filterType;
    private StringBuilder filterBody;

    protected Get(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Get processTag(final String tag, final Attributes attributes, final int level) {
        logger.trace("process tag, tag [{}], attributes # [{}], level [{}]", tag, attributes.getLength(), level);
        if (level == 2 && "filter".equalsIgnoreCase(tag)) {
            filterType = attributes.getValue("type");
            filterBody = new StringBuilder();
        } else if (level >= 3) {
            appendTagAttributes(attributes, tag);
        }
        return this;
    }

    private void appendTagAttributes(final Attributes attributes, final String tag) {
        filterBody.append("<").append(tag);
        for (int i = 0; i < attributes.getLength(); i++) {
            logger.trace("attribute: qName [{}] localName[{}] value [{}]",
                    attributes.getQName(i), attributes.getLocalName(i), attributes.getValue(i));
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
        logger.trace("processing end tag, tag [{}], level [{}]", tag, level);
        if (level == 2 && "filter".equalsIgnoreCase(tag)) {
            filter = createFilter(filterType, filterBody);
            filterType = null;
            filterBody = null;
        } else if (level > 2 && filterBody != null) {
            filterBody.append("</").append(tag).append(">");
        }
        return this;
    }

    private Filter createFilter(final String type, final StringBuilder body) {
        logger.trace("Creating filter, type [{}], body [{}]", type, body.toString());
        return new Filter() {
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
        }.init(type, body);
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        logger.trace("messageId [{}] filter [{}]", messageId, filter != null ? filter.asString() : "EMPTY!");
        commandListener.get(messageId, filter, out);
        return new Nop();
    }
}
