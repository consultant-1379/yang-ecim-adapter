/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import javax.xml.transform.TransformerException;

public class EmptyFilterProcessor implements FilterProcessor {

    @Override
    public String getFilterStringApplicableToNode() {
        return "";
    }

    @Override
    public String postProcess(final String result) throws TransformerException {
        return result;
    }

    @Override
    public boolean shouldTransform() {
        return false;
    }

}
