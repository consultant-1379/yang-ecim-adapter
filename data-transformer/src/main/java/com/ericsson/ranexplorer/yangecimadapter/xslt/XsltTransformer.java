/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import java.io.*;
import java.net.URL;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.TransformerFactoryImpl;

public class XsltTransformer {

    private static final Logger logger = LoggerFactory.getLogger(
            Thread.currentThread().getContextClassLoader().getClass());
    private static final TransformerFactory factory = new TransformerFactoryImpl();

    private Transformer transformer;

    public XsltTransformer(String styleSheetFileName) {
        logger.debug("Creating new XsltTransformer object for {}", styleSheetFileName);
        try {
            transformer = getTransformerForFile(styleSheetFileName);
        } catch (TransformerConfigurationException exception) {
            logger.error("Cannot create Transformer object for file {}", styleSheetFileName, exception);
        }
    }

    public XsltTransformer(String styleSheetFileName, String managedElementId, String templateType) {
        logger.debug("Creating new XsltTransformer object for {}", styleSheetFileName);
        try {
            transformer = getTransformerForFile(styleSheetFileName);
            transformer.setParameter("ManagedElement", managedElementId);
            transformer.setParameter("TemplateType", templateType);

        } catch (TransformerConfigurationException exception) {
            logger.error("Cannot create Transformer object for file {}", styleSheetFileName, exception);
        }
    }

    private Transformer getTransformerForFile(String styleSheetFileName)
            throws TransformerConfigurationException {
        return getTransformerFromStream(getStreamFromFile(styleSheetFileName));
    }

    private Transformer getTransformerFromStream(InputStream stream)
            throws TransformerConfigurationException {
        StreamSource streamSource = new StreamSource(stream);
        Transformer sTransformer = factory.newTransformer(streamSource);
        JarFileResolver jarFileResolver = new JarFileResolver();
        sTransformer.setURIResolver(jarFileResolver);
        return sTransformer;
    }

    private InputStream getStreamFromFile(String fileName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    }

    public String transform(final String xml) throws TransformerException {
        logger.debug("Transforming XML");
        logger.trace("XML string is:\n{}", xml);
        return transformXmlWithWriter(xml, new StringWriter());
    }

    public String transformWithDummyRootWrapper(final String xml) throws TransformerException {
        logger.debug("Transforming XML");
        logger.trace("XML string is:\n{}", xml);
        StringBuilder xmlString = new StringBuilder().append("<dummyRoot>").append(xml).append("</dummyRoot>");
        return transformXmlWithWriter(xmlString.toString(), new StringWriter());
    }

    private String transformXmlWithWriter(String xml, StringWriter writer)
            throws TransformerException {
        transformer.transform(getSourceFromString(xml), new StreamResult(writer));
        return writer.toString();
    }

    private Source getSourceFromString(String xml) {
        return new StreamSource(new StringReader(xml));
    }

    private class JarFileResolver implements URIResolver{

        public Source resolve(final String href, final String base)throws TransformerException{
            final URL url = getClass().getClassLoader().getResource(href);
            final StreamSource streamSource = new StreamSource();

            try {
                final InputStream jarFileIS = url.openStream();
                streamSource.setInputStream(jarFileIS);
            } catch (IOException e) {
                throw new TransformerException(e);
            }
            return streamSource;
        }
    }
}
