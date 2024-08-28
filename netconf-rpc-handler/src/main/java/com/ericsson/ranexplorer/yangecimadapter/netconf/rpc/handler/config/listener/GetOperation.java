/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener;

import com.ericsson.oss.mediation.util.netconf.api.*;
import com.ericsson.oss.mediation.util.netconf.api.exception.NetconfManagerException;
import com.ericsson.oss.mediation.util.netconf.filter.SubTreeFilter;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterProcessor;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterProcessorFactory;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public enum GetOperation {

    GET {
        @Override
        public NetconfResponse execute(final NetconfManager netconfManager, final SubTreeFilter subtreeFilter) throws NetconfManagerException {
            return netconfManager.get(subtreeFilter);
        }

        @Override
        public FilterProcessor getFilterProcessor(final Filter filter, final XsltTransformer xsltTransformer) {
            return FilterProcessorFactory.getFilterProcessor(filter, xsltTransformer);
        }
    },
    GET_CONFIG {
        @Override
        public NetconfResponse execute(final NetconfManager netconfManager, final SubTreeFilter subtreeFilter) throws NetconfManagerException {
            return netconfManager.getConfig(Datastore.RUNNING, subtreeFilter);
        }

        @Override
        public FilterProcessor getFilterProcessor(final Filter filter, final XsltTransformer xsltTransformer) {
            return FilterProcessorFactory.getFilterProcessorConfigDataOnly(filter, xsltTransformer);
        }
    };

    public NetconfResponse execute(final NetconfManager netconfManager, final SubTreeFilter subtreeFilter) throws NetconfManagerException {
        throw new AbstractMethodError();
    }

    public FilterProcessor getFilterProcessor(final Filter filter, final XsltTransformer xsltTransformer) {
        throw new AbstractMethodError();
    }

}
