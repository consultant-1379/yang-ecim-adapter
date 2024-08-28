/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt.util;

import static junit.framework.TestCase.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransformerTestHelper {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    private TransformerTestHelper() {
    }

    public static Collection<String> managedObjects(final String testFilesDir, final SchemaType schemaType) throws URISyntaxException {
        final List<String> managedObjectNames = new ArrayList<>();
        final String dirName = testFilesDir + File.separator + schemaType.getDirName();
        final File[] inputFiles = new File(ClassLoader.getSystemResource(dirName).toURI()).listFiles();
        final int fileExtensionLength = schemaType.getExtension().length();
        for (int i = 0; i < inputFiles.length; i++) {
            final String filename = inputFiles[i].getName();
            managedObjectNames.add(filename.substring(0, filename.length() - fileExtensionLength));
        }
        return managedObjectNames;
    }

    public static String getXmlFor(final String testFilesDir, final String managedObject, final SchemaType schemaType) {
        try {
            final Path path = getPath(testFilesDir, managedObject, schemaType);
            return new String(Files.readAllBytes(path));
        } catch (IOException | URISyntaxException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
            return "";
        }
    }

    private static Path getPath(final String testFilesDir, final String managedObject, final SchemaType schema) throws URISyntaxException {
        final String fullFileName = testFilesDir + File.separator + schema.getDirName() + File.separator + managedObject + schema.getExtension();
        return Paths.get(ClassLoader.getSystemResource(fullFileName).toURI());
    }

    public static String removeWhiteSpace(final String xmlString) {
        return xmlString.trim().replaceAll("\n|\r|\t", " ").replaceAll(" +", " ").replaceAll("> *<", "><");
    }
}
