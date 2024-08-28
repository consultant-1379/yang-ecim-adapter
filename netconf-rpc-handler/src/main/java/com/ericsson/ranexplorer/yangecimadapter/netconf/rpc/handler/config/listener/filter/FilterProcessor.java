/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import javax.xml.transform.TransformerException;

public interface FilterProcessor {
    
    String getFilterStringApplicableToNode();
    
    String postProcess(String result) throws TransformerException;
    
    boolean shouldTransform();

}