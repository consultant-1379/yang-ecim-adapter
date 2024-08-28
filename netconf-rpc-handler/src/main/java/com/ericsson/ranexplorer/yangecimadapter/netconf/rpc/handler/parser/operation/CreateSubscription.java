/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.oss.mediation.util.netconf.api.Filter;
import org.xml.sax.Attributes;


public class CreateSubscription extends Rpc {

    private String stream;
    private String startTime;
    private String stopTime;
    private Filter filter;
    private String filterType;
    private StringBuilder filterBody;

    public CreateSubscription(final Rpc rpc) {
        super(rpc);
    }

    @Override
    public Operation processTag(final String tag, final Attributes attributes, final int level) {
        if(level == 2){
            processL2Tags(tag, attributes);
        }else if(level > 2 && filterBody != null){
            appendTagAttributes(tag, attributes);
        }
        return this;
    }

    private void processL2Tags(String tag, Attributes attributes) {
        switch (tag.toLowerCase()) {
            case "stream":
                this.stream = attributes.getValue("stream");
                break;
            case "filter":
                filterType = attributes.getValue("type");
                filterBody = new StringBuilder();
                break;
            case "startTime":
                this.startTime = attributes.getValue("startTime");
                break;
            case "stopTime":
                this.stopTime = attributes.getValue("stopTime");
                break;
            default:
        }
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
        if(level == 2) {
            processL2EndTags(tag);
        } else if(level > 2 && filterBody != null) {
            filterBody.append("</").append(tag).append(">");
        }
        return this;
    }

    private void processL2EndTags(String tag) {
        if("filter".equalsIgnoreCase(tag)){
            filter  = getFilterInstance(filterType, filterBody);
            filterType = null;
            filterBody = null;
        }
    }

    private Filter getFilterInstance(final String filterType, final StringBuilder filterBody){
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
        }.init(filterType, filterBody);
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.createSubscription(messageId, stream, filter, startTime, stopTime, out);
        return new Nop();
    }
}
