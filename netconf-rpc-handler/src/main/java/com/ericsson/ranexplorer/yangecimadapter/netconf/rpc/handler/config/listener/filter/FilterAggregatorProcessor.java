/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import java.util.List;

import javax.xml.transform.TransformerException;

public class FilterAggregatorProcessor implements FilterProcessor {

    private List<FilterProcessor> filterProcessors;

    public FilterAggregatorProcessor(final List<FilterProcessor> filterProcessors) {
        this.filterProcessors = filterProcessors;
    }

    @Override
    public String getFilterStringApplicableToNode() {
        StringBuilder stringBuilder = new StringBuilder();
        for (FilterProcessor filterProcessor : filterProcessors) {
            final String filterString = filterProcessor.getFilterStringApplicableToNode();
            if (isEmpty(filterString)) {
                continue;
            }
            stringBuilder = stringBuilder.length()==0 ? stringBuilder.append(filterString) : stringBuilder.append("\n").append(filterString);
        }
        return stringBuilder.toString();
    }

    @Override
    public String postProcess(String result) throws TransformerException {
        String processedResult = result;
        for (FilterProcessor filterProcessor : filterProcessors) {
            processedResult = filterProcessor.postProcess(processedResult);
        }
        return processedResult;
    }

    @Override
    public boolean shouldTransform() {
        for (FilterProcessor filterProcessor : filterProcessors) {
            if(filterProcessor.shouldTransform()){
                return true;
            }
        }
        return false;
    }

    private boolean isEmpty(final String input) {
        return input.trim().isEmpty();
    }

}
