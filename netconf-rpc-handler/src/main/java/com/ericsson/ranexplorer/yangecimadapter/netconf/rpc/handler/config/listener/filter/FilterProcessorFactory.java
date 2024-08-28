/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.mediation.util.netconf.api.Filter;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.block.*;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class FilterProcessorFactory {

    private FilterProcessorFactory() {
    }

    public static FilterProcessor getFilterProcessor(final Filter filter, final XsltTransformer xsltTransformer) {
        FilterProcessor filterProcessor;
        if (isNullFilter(filter)) {
            final List<FilterProcessor> filterProcessors = new ArrayList<>();
            filterProcessors.add(new EnodebfunctionDefaultFilterProcessorAllData(xsltTransformer));
            filterProcessors.add(new NodeSupportDefaultFilterProcessorAllData());
            filterProcessors.add(new EquipmentDefaultFilterProcessorAllData(xsltTransformer));
            filterProcessors.add(new NetconfStateFilterProcessor(true, true, true));
            filterProcessors.add(new EventStreamFilterProcessor(true));
            filterProcessor = new FilterAggregatorProcessor(filterProcessors);
        } else if (isEmpty(filter)) {
            filterProcessor = new EmptyFilterProcessor();
        } else {
            final FilterBlockHandler filterBlockHandler = new FilterBlockHandlerAllData();
            final List<FilterProcessor> filterProcessors = filterBlockHandler.getFilterProcessorsFromFilter(filter.asString(), xsltTransformer);
            filterProcessor = new FilterAggregatorProcessor(moveDefaultFiltersToStart(filterProcessors));
        }
        return filterProcessor;
    }

    public static FilterProcessor getFilterProcessorCreateSubscription(final Filter filter) {
        if(isNullFilter(filter) || isEmpty(filter)){
            return new EmptyFilterProcessor();
        }
        return new SimpleFilterProcessorCreateSubscription(filter.asString());
    }

    public static FilterProcessor getFilterProcessorConfigDataOnly(final Filter filter, final XsltTransformer xsltTransformer) {
        FilterProcessor filterProcessor;
        if (isNullFilter(filter)) {
            final List<FilterProcessor> filterProcessors = new ArrayList<>();
            filterProcessors.add(new EnodebfunctionDefaultFilterProcessorConfigData(xsltTransformer));
            filterProcessors.add(new NodeSupportDefaultFilterProcessorConfigData());
            filterProcessors.add(new EquipmentDefaultFilterProcessorConfigData(xsltTransformer));
            filterProcessor = new FilterAggregatorProcessor(filterProcessors);
        } else if (isEmpty(filter)) {
            filterProcessor = new EmptyFilterProcessor();
        } else {
            final FilterBlockHandler filterBlockHandler = new FilterBlockHandlerConfigData();
            final List<FilterProcessor> filterProcessors = filterBlockHandler.getFilterProcessorsFromFilter(filter.asString(), xsltTransformer);
            filterProcessor = new FilterAggregatorProcessor(moveDefaultFiltersToStart(filterProcessors));
        }
        return filterProcessor;
    }

    private static boolean isNullFilter(final Filter filter) {
        return filter == null || filter.asString() == null;
    }

    private static boolean isEmpty(final Filter filter) {
        return filter.asString().trim().isEmpty();
    }

    private static List<FilterProcessor> moveDefaultFiltersToStart(final List<FilterProcessor> filterProcessors) {
        // Default Filter runs a xslt transformation so needs to be executed first in post-process
        final List<FilterProcessor> result = new ArrayList<>();

        for (final FilterProcessor filterProcessor : filterProcessors) {
            if (filterProcessor instanceof DefaultFilterProcessor) {
                result.add(filterProcessor);
            }
        }

        for (final FilterProcessor filterProcessor : filterProcessors) {
            if (!(filterProcessor instanceof DefaultFilterProcessor)) {
                result.add(filterProcessor);
            }
        }
        return result;
    }
}
