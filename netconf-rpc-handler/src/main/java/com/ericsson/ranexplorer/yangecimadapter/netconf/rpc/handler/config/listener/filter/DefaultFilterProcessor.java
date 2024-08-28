/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import javax.xml.transform.TransformerException;

import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public abstract class DefaultFilterProcessor implements FilterProcessor {

    private XsltTransformer xsltTransformer;

    public DefaultFilterProcessor(final XsltTransformer xsltTransformer) {
        this.xsltTransformer = xsltTransformer;
    }

    @Override
    public String postProcess(final String result) throws TransformerException {
        return xsltTransformer.transformWithDummyRootWrapper(result).replaceAll("(<[a-z].*/>\n)", "");
    }

    @Override
    public boolean shouldTransform() {
        return true;
    }

}
