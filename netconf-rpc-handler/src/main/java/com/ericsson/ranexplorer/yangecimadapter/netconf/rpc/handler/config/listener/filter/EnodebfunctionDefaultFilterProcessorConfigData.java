/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterBlockName.*;

import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class EnodebfunctionDefaultFilterProcessorConfigData extends DefaultFilterProcessor {

    public EnodebfunctionDefaultFilterProcessorConfigData(final XsltTransformer xsltTransformer) {
        super(xsltTransformer);
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return DefaultFilterProvider.getInstance().getDefaultFilterConfigDataOnly(ENODEB_FUNCTION);
    }

}
