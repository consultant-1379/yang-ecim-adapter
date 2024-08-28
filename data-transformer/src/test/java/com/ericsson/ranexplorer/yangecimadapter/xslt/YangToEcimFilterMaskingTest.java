/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import static com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper.*;
import static junit.framework.TestCase.fail;

import com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper;
import com.ericsson.ranexplorer.yangecimadapter.xslt.util.YangToEcim;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.net.URISyntaxException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class YangToEcimFilterMaskingTest {

    private static final Logger logger  = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    private static final String TEST_FILES_DIR = "yangToEcimMasking";

    private final XsltTransformer transformer = XsltTransformerFactory.newYangToEcimTransformer("Test", "subscription");
    private final String managedObject;

    public YangToEcimFilterMaskingTest(final String managedObject){
        this.managedObject = managedObject;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<String> managedObjects() throws URISyntaxException{
        return TransformerTestHelper.managedObjects(TEST_FILES_DIR, YangToEcim.YANG);
    }

    @Test
    public void testFilterMasking(){
        try{
            final String yangXmlIn = getXmlFor(TEST_FILES_DIR, managedObject, YangToEcim.YANG);
            final String ecimXmlOut = removeWhiteSpace(getXmlFor(TEST_FILES_DIR, managedObject, YangToEcim.ECIM));
            TestCase.assertEquals(ecimXmlOut, transformer.transformWithDummyRootWrapper(yangXmlIn).replaceAll("[\t\r\n]", ""));
        }catch (final TransformerException exception){
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }
}
