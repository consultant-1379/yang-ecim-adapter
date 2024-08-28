/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterBlockName.*;

import javax.xml.transform.TransformerException;

public class NodeSupportDefaultFilterProcessorAllData implements FilterProcessor {

    @Override
    public String getFilterStringApplicableToNode() {
        return DefaultFilterProvider.getInstance().getDefaultFilter(NODE_SUPPORT);
    }

    @Override
    public String postProcess(final String result) throws TransformerException {
        return result.replaceAll("(<[a-z].*/>\\s)", "");
    }

    @Override
    public boolean shouldTransform() {
        return true;
    }

}
