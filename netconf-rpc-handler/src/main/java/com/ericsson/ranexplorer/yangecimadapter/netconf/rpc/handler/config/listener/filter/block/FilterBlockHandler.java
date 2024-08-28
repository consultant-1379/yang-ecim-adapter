/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.block;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter.*;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.exceptions.FilterProcessingException;
import com.ericsson.ranexplorer.yangecimadapter.xslt.XsltTransformer;

public abstract class FilterBlockHandler {

    private static final Logger logger = LoggerFactory.getLogger(FilterBlockHandler.class);

    private static final String DUMMY_ROOT = "dummyRoot";
    private static final String DUMMY_ROOT_START_ELEMENT = "<" + DUMMY_ROOT + ">";
    private static final String DUMMY_ROOT_END_ELEMENT = "</" + DUMMY_ROOT + ">";

    public List<FilterProcessor> getFilterProcessorsFromFilter(final String input, final XsltTransformer xsltTransformer) {
        final String filterString = appendDummyRoot(input);
        final List<FilterProcessor> filterProcessors = new ArrayList<>();

        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new ByteArrayInputStream(filterString.getBytes("UTF-8")));
            while (xmlEventReader.hasNext()) {
                final XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    final StartElement startElement = xmlEvent.asStartElement();
                    final String localName = startElement.getName().getLocalPart();
                    if (DUMMY_ROOT.equals(localName)) {
                        continue;
                    }

                    final FilterBlockName filterBlockName = FilterBlockName.fromTagName(localName);
                    final FilterProcessor filterProcessor = getFilterProcessor(filterBlockName, xmlEventReader, startElement, xsltTransformer);
                    filterProcessors.add(filterProcessor);
                }

            }
        } catch (XMLStreamException | UnsupportedEncodingException exception) {
            logger.error("Exception thrown while getting the filter processors from filter", exception);
            throw new FilterProcessingException(exception.getMessage());
        }
        return filterProcessors;
    }

    protected FilterProcessor getFilterProcessor(final XMLEventReader xmlEventReader, final StartElement startElement,
                                                 final FilterBlockName filterBlockName, final XsltTransformer xsltTransformer)
            throws XMLStreamException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<").append(startElement.getName().getLocalPart()).append(">");
        int level = 1;
        int maxLevel = 1;
        while (xmlEventReader.hasNext() && level > 0) {
            final XMLEvent xmlEvent = xmlEventReader.nextEvent();

            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    stringBuilder.append("<").append(xmlEvent.asStartElement().getName().getLocalPart()).append(">");
                    ++level;
                    ++maxLevel;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    stringBuilder.append("</").append(xmlEvent.asEndElement().getName().getLocalPart()).append(">");
                    --level;
                    break;

                default:
                    stringBuilder.append(xmlEvent.toString());

            }
        }

        if (maxLevel > 1) {
            return new SimpleFilterProcessor(stringBuilder.toString());
        }
        return getDefaultFilterProcessor(filterBlockName, xsltTransformer);
    }

    private String appendDummyRoot(final String input) {
        return DUMMY_ROOT_START_ELEMENT + input + DUMMY_ROOT_END_ELEMENT;
    }

    protected FilterProcessor handleAsEmptyFilterProcessor(final XMLEventReader xmlEventReader) throws XMLStreamException {
        // continue to end of block and return EmptyFilterProcessor
        int level = 1;
        while (xmlEventReader.hasNext() && level > 0) {
            final XMLEvent xmlEvent = xmlEventReader.nextEvent();

            switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    ++level;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    --level;
                    break;

                default:
                    break;
            }
        }

        return new EmptyFilterProcessor();
    }

    protected abstract FilterProcessor getDefaultFilterProcessor(FilterBlockName filterBlockName, XsltTransformer xsltTransformer);

    protected abstract FilterProcessor getFilterProcessor(FilterBlockName filterBlockName, XMLEventReader xmlEventReader, StartElement startElement,
                                                          XsltTransformer xsltTransformer)
            throws XMLStreamException;

}
