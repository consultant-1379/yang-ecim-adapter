/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import static junit.framework.TestCase.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeDefaultFilterResultsTest {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());
    private static final String PARENT_DIR = "mergeDefaultFilterResults";
    private static final String IN_DIR = "in";
    private static final String OUT_DIR = "out";

    @Test
    public void testTransform_duplicatedMOsIn_mergedMOsOut() { //NOSONAR
        try {
            final String xmlIn = getXmlFrom(getFilename(PARENT_DIR, IN_DIR, "YangWithDuplicatedMOs.xml"));
            final XsltTransformer transformer = XsltTransformerFactory.newMergeResultsDefaultFilterTransformer();
            final String result = transformer.transformWithDummyRootWrapper(xmlIn);

            final String expectedOut = removeWhiteSpace(getXmlFrom(getFilename(PARENT_DIR, OUT_DIR, "YangWithMergedMOs.xml")));
            assertEquals(expectedOut, removeWhiteSpace(result));
        } catch (final TransformerException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testTransform_ENodeBFunction_mergedMOsOut() { //NOSONAR
        try {
            final String xmlIn = getXmlFrom(getFilename(PARENT_DIR, IN_DIR, "ENodeBFunctionWithDuplicates.xml"));
            final XsltTransformer transformer = XsltTransformerFactory.newMergeResultsDefaultFilterTransformer();
            final String result = transformer.transformWithDummyRootWrapper(xmlIn);

            final String expectedOut = removeWhiteSpace(getXmlFrom(getFilename(PARENT_DIR, OUT_DIR, "ENodeBFunctionMerged.xml")));
            assertEquals(expectedOut, removeWhiteSpace(result));
        } catch (final TransformerException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testTransform_Equipment_mergedMOsOut() { //NOSONAR
        try {
            final String xmlIn = getXmlFrom(getFilename(PARENT_DIR, IN_DIR, "EquipmentWithDuplicates.xml"));
            final XsltTransformer transformer = XsltTransformerFactory.newMergeResultsDefaultFilterTransformer();
            final String result = transformer.transformWithDummyRootWrapper(xmlIn);

            final String expectedOut = removeWhiteSpace(getXmlFrom(getFilename(PARENT_DIR, OUT_DIR, "EquipmentMerged.xml")));
            assertEquals(expectedOut, removeWhiteSpace(result));
        } catch (final TransformerException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    private String getXmlFrom(final String filename) {
        try {
            final Path path = getPath(filename);
            return new String(Files.readAllBytes(path));
        } catch (IOException | URISyntaxException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
            return "";
        }
    }

    private Path getPath(final String filename) throws URISyntaxException {
        return Paths.get(ClassLoader.getSystemResource(filename).toURI());
    }

    private String removeWhiteSpace(final String xmlString) {
        final char[] result = new char[xmlString.length()];
        final char[] input = xmlString.toCharArray();
        boolean keep = false;
        int j = 0;
        for (int i = 0; i < input.length; i++) {
            if (input[i] == '<') {
                keep = true;
            } else if (input[i] == '>') {
                keep = false;
            }

            if (isNotWhiteSpace(input[i]) || keep) {
                result[j] = input[i];
                j++;
            }
        }
        return String.valueOf(result).trim();
    }

    private boolean isNotWhiteSpace(final char ch) {
        return !Character.isWhitespace(ch);
    }

    private static String getFilename(final String dir1, final String dir2, final String filename) {
        return dir1 + File.separator + dir2 + File.separator + filename;
    }

}
