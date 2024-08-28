/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.oss.mediation.util.netconf.manager;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.constants.TestConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.transport.api.TransportManager;
import com.ericsson.oss.mediation.util.netconf.api.*;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.*;
import com.ericsson.oss.mediation.util.netconf.api.error.*;
import com.ericsson.oss.mediation.util.netconf.api.error.Error;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.XmlFileReaderException;

public class NetconfManagerStub extends NetconfManagerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfManagerStub.class);
    private static final String ENODEB_FUNCTION_ID_TAG = "<eNodeBFunctionId>1</eNodeBFunctionId></ENodeBFunction>";

    private static final Map<String, Object> configProperties = new HashMap<>();

    static {
        configProperties.put("capabilities", new ArrayList<String>());
    }

    public NetconfManagerStub(TransportManager transportManager) throws NetconfManagerException {
        super(transportManager, configProperties);
    }

    public NetconfManagerStub(TransportManager transportManager, Map<String, Object> configProperties) throws NetconfManagerException {
        super(transportManager, configProperties);
    }

    @Override
    public NetconfResponse connect() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse disconnect() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfConnectionStatus getStatus() {
        return null;
    }

    @Override
    public NetconfResponse get() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse get(Filter filter) {
        final NetconfResponse netconfResponse = new NetconfResponse();
        final String filterString = filter.asString();

        try {
            if (filterString.contains("TestDefaultFilter") && filterString.contains("<qci/>") || filterString.contains(ENODEB_FUNCTION_ID_TAG)) {
                netconfResponse.setData(ECIM_NULL_FILTER_RESULT);
                netconfResponse.setError(false);
            } else if(filterString.contains("EquipmentDefaultFilterTest")){
                netconfResponse.setData(EXPECTED_ECIM_EQUIPMENT_RESULT);
                netconfResponse.setError(false);
            } else if (filterString.contains("mcc")) {
                netconfResponse.setData(ECIM_RESULT_WITH_MCC);
                netconfResponse.setError(false);
            } else if (filterString.contains("unexpectedValue")) {
                netconfResponse.setError(true);
                Error error = new Error();
                error.setErrortype(ErrorType.application);
                error.setErrorTag(ErrorTag.DATA_MISSING);
                error.setErrorSeverity(ErrorSeverity.error);
                error.setErrorMessage("Invalid value for qciProfilePredefinedId");
                netconfResponse.setErrors(Arrays.asList(error));
            }
            return netconfResponse;
        } catch (XmlFileReaderException exception) {
            LOG.error("Error occurred initializing default filter: {} ", exception.getMessage(), exception);
            netconfResponse.setError(true);
            netconfResponse.setErrorMessage(exception.getMessage());
            return netconfResponse;
        }
    }

    @Override
    public NetconfResponse getConfig() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse getConfig(Datastore source) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse getConfig(Datastore source, Filter filter) throws NetconfManagerException {
        final NetconfResponse netconfResponse = new NetconfResponse();
        final String filterString = filter.asString();

        try {
            if (filterString.contains("TestDefaultFilter") && !filterString.contains("<qci/>") || filterString.contains(ENODEB_FUNCTION_ID_TAG)) {
                netconfResponse.setData(ECIM_NULL_FILTER_RESULT);
                netconfResponse.setError(false);
            }
            return netconfResponse;
        } catch (XmlFileReaderException exception) {
            LOG.error("Error occurred initializing default filter: {} ", exception.getMessage(), exception);
            netconfResponse.setError(true);
            netconfResponse.setErrorMessage(exception.getMessage());
            return netconfResponse;
        }
    }

    @Override
    public NetconfResponse get(NetconfResponseListener listener, Datastore source, Filter filter) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse getConfig(NetconfResponseListener listener, Datastore source, Filter filter) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse lock(Datastore target) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse unlock(Datastore target) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse validate(String config) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse validate(Datastore source) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse commit() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse discardChanges() throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, String config) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, DefaultOperation defaultOperation, String config) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, ErrorOption errorOption, String config) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, TestOption testOption, String config) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, ErrorOption errorOption, TestOption testOption, String config)
            throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse editConfig(Datastore target, DefaultOperation defaultOperation, ErrorOption errorOption, TestOption testOption,
                                      String config)
            throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse killSession(String sessionId) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse createSubscription(String stream, Filter filter, String startTime, String stopTime) throws NetconfManagerException {
        final NetconfResponse netconfResponse = new NetconfResponse();
        netconfResponse.setError(false);
        return netconfResponse;
    }

    @Override
    public Collection<Capability> getAllActiveCapabilities() {
        List<Capability> capabilities = new ArrayList<>();
        try {
            capabilities.add(new Capability(new URI("urn:ietf:params:netconf:base:1.0")));
            capabilities.add(new Capability(new URI("urn:ietf:params:netconf:capability:candidate:1.0")));
            capabilities.add(new Capability(new URI("urn:com:ericsson:ebase:1.1.0")));
            capabilities.add(new Capability(new URI("urn:com:ericsson:ebase:1.2.0")));

        } catch (URISyntaxException exception) {
            LOG.error("Exception thrown while trying to get all active capabilities", exception);
        }
        return capabilities;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public NetconfResponse action(String actionMessage) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse action(String actionNamespace, String actionMessage) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse copyConfig(String source, String target) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse customOperation(String requestBody) throws NetconfManagerException {
        return null;
    }

    @Override
    public NetconfResponse customOperation(String requestBody, boolean returnResponse) throws NetconfManagerException {
        return null;
    }

}
