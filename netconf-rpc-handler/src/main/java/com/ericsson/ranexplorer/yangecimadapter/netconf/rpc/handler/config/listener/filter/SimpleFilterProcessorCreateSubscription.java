/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

public class SimpleFilterProcessorCreateSubscription implements FilterProcessor {

    private String filter;

    public SimpleFilterProcessorCreateSubscription(final String filter) {
        this.filter = filter;
    }

    @Override
    public String getFilterStringApplicableToNode() {
        return filter;
    }

    @Override
    public String postProcess(final String result) {
        return result;
    }

    @Override
    public boolean shouldTransform() {
        return false;
    }

}
