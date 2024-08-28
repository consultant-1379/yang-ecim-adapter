/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterBlockName.*;

import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class EnodebfunctionDefaultFilterProcessorAllData extends DefaultFilterProcessor {

    public EnodebfunctionDefaultFilterProcessorAllData(final XsltTransformer xsltTransformer) {
        super(xsltTransformer);
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return DefaultFilterProvider.getInstance().getDefaultFilter(ENODEB_FUNCTION);
    }

}
