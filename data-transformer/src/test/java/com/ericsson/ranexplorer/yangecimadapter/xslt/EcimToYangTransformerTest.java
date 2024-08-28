/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import static com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper.*;
import static junit.framework.TestCase.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.xslt.util.*;

@RunWith(Parameterized.class)
public class EcimToYangTransformerTest {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    private static final String TEST_FILES_DIR = "ecimToYang";

    private String ecimFile;
    private String yangFile;
    private XsltTransformer transformer;

    @Before
    public void initialize() {
        transformer = XsltTransformerFactory.newEcimToYangTransformer();
    }

    public EcimToYangTransformerTest(String ecimFile, String yangFile) {
        this.ecimFile = ecimFile;
        this.yangFile = yangFile;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<String[]> dataPaths() throws URISyntaxException {
        final List<String[]> dataPaths = new ArrayList<>();

        final String dirName = TEST_FILES_DIR + File.separator + "in";
        final File[] inputFiles = new File(ClassLoader.getSystemResource(dirName).toURI()).listFiles();
        final int fileExtensionLength = EcimToYang.ECIM.getExtension().length();

        for (int i = 0; i < inputFiles.length; i++) {
            final String filename = inputFiles[i].getName();
            final String managedObject = filename.substring(0, filename.length() - fileExtensionLength);
            String [] paths = {getFullFileName(managedObject, EcimToYang.ECIM), getFullFileName(managedObject, EcimToYang.YANG)};
            dataPaths.add(paths);
        }
        return dataPaths;
    }

    private static String getFullFileName(final String managedObject, final SchemaType schema){
        return TEST_FILES_DIR + File.separator + schema.getDirName() + File.separator + managedObject + schema.getExtension();
    }

    @Test
    public void testEcimToYangTransform() {
        logger.debug("testing input file {} against expected file {}", ecimFile, yangFile);

        try {
            String yangXml = new String( Files.readAllBytes( Paths.get( ClassLoader
                    .getSystemResource(yangFile).toURI() )));
            String ecimXml = new String( Files.readAllBytes( Paths.get( ClassLoader
                    .getSystemResource(ecimFile).toURI() )));

            assertEquals(removeWhiteSpace(yangXml), removeWhiteSpace(transformer.transform(ecimXml)));

        } catch (IOException | URISyntaxException | TransformerException exception) {
            logger.error(exception.getMessage(),exception);
            fail();
        }
    }

}
