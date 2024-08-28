/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.common.services.util.AdapterUtils;

public class EventStreamFilterProcessor implements FilterProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EventStreamFilterProcessor.class);

    private static String streams = "";

    static {
        try {
            streams = AdapterUtils.readFileToString("/eventStreams.xml");
        } catch (IOException exception) {
            logger.error("Exception while trying to read the eventStreams.xml file", exception);
        }
    }

    private boolean includeStreams;

    public EventStreamFilterProcessor(final boolean includeStreams) {
        this.includeStreams = includeStreams;
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return "";
    }

    @Override
    public String postProcess(String result) throws TransformerException {
        if (!includeStreams) {
            return result;
        }
        return isEmpty(result) ? streams : result + "\n" + streams;
    }

    @Override
    public boolean shouldTransform() {
        return false;
    }

    private boolean isEmpty(final String input) {
        return input.trim().isEmpty();
    }

}
