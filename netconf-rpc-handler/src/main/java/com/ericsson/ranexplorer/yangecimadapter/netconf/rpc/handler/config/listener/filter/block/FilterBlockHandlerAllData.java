/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.block;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.*;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public class FilterBlockHandlerAllData extends FilterBlockHandler {

    private static final String CAPABILITIES = "capabilities";
    private static final String DATASTORES = "datastores";
    private static final String SCHEMAS = "schemas";
    private static final String STREAMS = "streams";

    @Override
    protected FilterProcessor getDefaultFilterProcessor(final FilterBlockName filterBlockName, final XsltTransformer xsltTransformer) {
        FilterProcessor filterProcessor;
        switch (filterBlockName) {
            case ENODEB_FUNCTION:
                filterProcessor = new EnodebfunctionDefaultFilterProcessorAllData(xsltTransformer);
                break;

            case NODE_SUPPORT:
                filterProcessor = new NodeSupportDefaultFilterProcessorAllData();
                break;

            case EQUIPMENT:
                filterProcessor = new EquipmentDefaultFilterProcessorAllData(xsltTransformer);
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

        FilterProcessor filterProcessor;
        switch (filterBlockName) {
            case ENODEB_FUNCTION:
            case NODE_SUPPORT:
            case EQUIPMENT:
                filterProcessor = super.getFilterProcessor(xmlEventReader, startElement, filterBlockName, xsltTransformer);
                break;

            case NETCONF_STATE:
                filterProcessor = getNetconfStateFilterProcessor(xmlEventReader);
                break;

            case EVENT_STREAM:
                filterProcessor = getEventStreamFilterProcessor(xmlEventReader);
                break;

            case UNKNOWN:
            default:
                filterProcessor = super.handleAsEmptyFilterProcessor(xmlEventReader);
                break;
        }
        return filterProcessor;
    }

    protected FilterProcessor getNetconfStateFilterProcessor(final XMLEventReader xmlEventReader) throws XMLStreamException {
        int level = 1;
        int maxLevel = 1;
        boolean containsCapabilities = false;
        boolean containsDatastores = false;
        boolean containsSchemas = false;
        while (xmlEventReader.hasNext() && level > 0) {
            final XMLEvent xmlEvent = xmlEventReader.nextEvent();

            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    ++level;
                    ++maxLevel;
                    final String localName = xmlEvent.asStartElement().getName().getLocalPart();
                    if (CAPABILITIES.equals(localName)) {
                        containsCapabilities = true;
                    } else if (DATASTORES.equals(localName)) {
                        containsDatastores = true;
                    } else if (SCHEMAS.equals(localName)) {
                        containsSchemas = true;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    --level;
                    break;

                default:
                    break;
            }
        }

        if (maxLevel > 1) {
            return new NetconfStateFilterProcessor(containsCapabilities, containsDatastores, containsSchemas);
        }
        return new NetconfStateFilterProcessor(true, true, true);
    }

    protected FilterProcessor getEventStreamFilterProcessor(final XMLEventReader xmlEventReader) throws XMLStreamException {
        int level = 1;
        boolean containsStreams = false;
        while (xmlEventReader.hasNext() && level > 0) {
            final XMLEvent xmlEvent = xmlEventReader.nextEvent();

            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    ++level;
                    final String localName = xmlEvent.asStartElement().getName().getLocalPart();
                    if (STREAMS.equals(localName)) {
                        containsStreams = true;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    --level;
                    break;

                default:
                    break;
            }
        }

        return new EventStreamFilterProcessor(containsStreams);
    }

}
