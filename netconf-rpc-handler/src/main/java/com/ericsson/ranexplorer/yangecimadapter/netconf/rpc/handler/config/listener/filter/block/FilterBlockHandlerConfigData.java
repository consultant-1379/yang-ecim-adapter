/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.block;

import static com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.FilterBlockName.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.*;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class FilterBlockHandlerConfigData extends FilterBlockHandler {

    @Override
    protected FilterProcessor getDefaultFilterProcessor(final FilterBlockName filterBlockName, final XsltTransformer xsltTransformer) {
        FilterProcessor filterProcessor;
        switch (filterBlockName) {
            case ENODEB_FUNCTION:
                filterProcessor = new EnodebfunctionDefaultFilterProcessorConfigData(xsltTransformer);
                break;

            case NODE_SUPPORT:
                filterProcessor = new NodeSupportDefaultFilterProcessorConfigData();
                break;

            case EQUIPMENT:
                filterProcessor = new EquipmentDefaultFilterProcessorConfigData(xsltTransformer);
                break;

            default:
                filterProcessor = new EmptyFilterProcessor();
                break;
        }
        return filterProcessor;
    }

    @Override
    protected FilterProcessor getFilterProcessor(final FilterBlockName filterBlockName, final XMLEventReader xmlEventReader,
                                                 final StartElement startElement, final XsltTransformer xsltTransformer)
            throws XMLStreamException {

        if (ENODEB_FUNCTION.equals(filterBlockName) || NODE_SUPPORT.equals(filterBlockName) || EQUIPMENT.equals(filterBlockName)) {
            return super.getFilterProcessor(xmlEventReader, startElement, filterBlockName, xsltTransformer);
        } else {
            // Block is not config data, continue to end of block and return EmptyFilterProcessor
            return super.handleAsEmptyFilterProcessor(xmlEventReader);
        }
    }

}
