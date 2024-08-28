/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.common.services.netconf.capabilities.CapabilityService;
import com.ericsson.ranexplorer.yangecimadapter.common.services.util.AdapterUtils;

public class NetconfStateFilterProcessor implements FilterProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NetconfStateFilterProcessor.class);

    private static final String NETCONF_STATE = "<netconf-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring\">\n%s</netconf-state>";
    private static final String DATASTORES_NODE = "<datastores><datastore><name>running</name></datastore></datastores>\n";

    private boolean includeCapabilities;
    private boolean includeDatastores;
    private boolean includeSchemas;

    public NetconfStateFilterProcessor(boolean capabilities, boolean datastores, boolean schemas) {
        this.includeCapabilities = capabilities;
        this.includeDatastores = datastores;
        this.includeSchemas = schemas;
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return "";
    }

    @Override
    public String postProcess(final String result) throws TransformerException {
        return isEmpty(result) ? getNetconfStateResponse() : result + "\n" + getNetconfStateResponse();
    }

    @Override
    public boolean shouldTransform() {
        return false;
    }

    private String getNetconfStateResponse() {
        String response = "";
        if (includeCapabilities) {
            response = getMonitoringCapabilities();
        }
        if (includeDatastores) {
            response = response + DATASTORES_NODE;
        }
        if (includeSchemas) {
            response = response + getMonitoringSchemas();
        }
        return getNetconfStat(response);
    }

    private String getMonitoringCapabilities() {
        CapabilityService capabilityService = new CapabilityService();
        return "<capabilities>" + capabilityService.getCapabilitiesAsXML("capabilities.to.client") + "</capabilities>\n";
    }

    private String getMonitoringSchemas() {
        String result = "";
        try {
            result = AdapterUtils.readFileToString("/schemas.xml") + "\n";
        } catch (IOException exception) {
            logger.error("Exception while trying to read the schemas file", exception);
        }
        return result;
    }

    private String getNetconfStat(String msg) {
        return String.format(NETCONF_STATE, msg);
    }

    private boolean isEmpty(final String input) {
        return input.trim().isEmpty();
    }

}
