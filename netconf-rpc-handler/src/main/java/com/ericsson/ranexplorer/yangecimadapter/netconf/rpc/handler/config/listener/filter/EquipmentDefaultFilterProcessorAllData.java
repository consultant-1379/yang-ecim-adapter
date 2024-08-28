/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class EquipmentDefaultFilterProcessorAllData extends DefaultFilterProcessor{

    public EquipmentDefaultFilterProcessorAllData(final XsltTransformer xsltTransformer) {
        super(xsltTransformer);
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return DefaultFilterProvider.getInstance().getDefaultFilter(FilterBlockName.EQUIPMENT);
    }
}
