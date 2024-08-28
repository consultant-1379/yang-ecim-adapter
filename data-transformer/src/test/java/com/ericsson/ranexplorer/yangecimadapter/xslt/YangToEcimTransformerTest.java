/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import static com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper.*;
import static junit.framework.TestCase.*;

import java.net.URISyntaxException;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper;
import com.ericsson.ranexplorer.yangecimadapter.xslt.util.YangToEcim;

@RunWith(Parameterized.class)
public class YangToEcimTransformerTest {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    private static final String TEST_FILES_DIR = "yangToEcim";

    private final XsltTransformer transformer = XsltTransformerFactory.newYangToEcimTransformer("1", "default");
    private final String managedObject;

    public YangToEcimTransformerTest(final String managedObject) {
        this.managedObject = managedObject;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<String> managedObjects() throws URISyntaxException {
        return TransformerTestHelper.managedObjects(TEST_FILES_DIR, YangToEcim.YANG);
    }

    @Test
    public void testTransform() {
        try {
            final String yangXmlIn = getXmlFor(TEST_FILES_DIR, managedObject, YangToEcim.YANG);
            final String ecimXmlOutput = removeWhiteSpace(getXmlFor(TEST_FILES_DIR, managedObject, YangToEcim.ECIM));
            assertEquals(ecimXmlOutput, removeWhiteSpace(transformer.transformWithDummyRootWrapper(yangXmlIn)));
        } catch (final TransformerException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

}
