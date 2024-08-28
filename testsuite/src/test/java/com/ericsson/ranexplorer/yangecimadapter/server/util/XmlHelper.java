/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ericsson.ranexplorer.yangecimadapter.server.exceptions.ParsingFailedException;

public class XmlHelper {

    private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    private XmlHelper() {

    }

    public static String getValue(final String xmlString, final String nodeName) {
        final Document document = getXmlAsDoc(xmlString);
        final Node node = document.getElementsByTagName(nodeName).item(0);
        return getTextValue(node);
    }

    private static Document getXmlAsDoc(final String xmlString) {
        Document resultDocument = null;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = factory.newDocumentBuilder();
            final InputSource is = new InputSource(new StringReader(xmlString));
            resultDocument = docBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            logger.error("Exception thrown while trying to read xml string {}", xmlString, exception);
            throw new ParsingFailedException("Failed to parse xml", exception);
        }
        return resultDocument;
    }

    private static String getTextValue(final Node node) {
        String result = null;
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                result = childNode.getNodeValue();
            }
        }
        return result;
    }

    public static String getXmlWithoutWhitespace(final String xmlString) {
        return xmlString.trim().replaceAll("\n|\r|\t", " ").replaceAll(" +", " ").replaceAll("> *<",
                "><");
    }

    public static String getStringWithoutWhitespace(final String xmlString) {
        return xmlString.replaceAll("[\t|\r|\n|\\s]", "");
    }

    public static String stripRpcReplyAndDataTags(String xmlString) {
        return xmlString.replaceAll("<\\?xml[ ].*>|<rpc-reply[ ].*>|<data>|</data>|</rpc-reply>", "").trim();
    }

}
