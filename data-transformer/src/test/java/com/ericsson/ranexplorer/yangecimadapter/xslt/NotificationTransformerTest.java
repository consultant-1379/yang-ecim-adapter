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

import com.ericsson.ranexplorer.yangecimadapter.xslt.util.EcimToYang;
import com.ericsson.ranexplorer.yangecimadapter.xslt.util.TransformerTestHelper;

@RunWith(Parameterized.class)
public class NotificationTransformerTest {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    private static final String TEST_FILES_DIR = "notification";

    private final XsltTransformer transformer = XsltTransformerFactory.newNotificationTransformer();
    private final String managedObject;

    public NotificationTransformerTest(final String managedObject) {
        this.managedObject = managedObject;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<String> managedObjects() throws URISyntaxException {
        return TransformerTestHelper.managedObjects(TEST_FILES_DIR, EcimToYang.ECIM);
    }

    @Test
    public void testTransform() {
        try {
            final String ecimNotificationIn = getXmlFor(TEST_FILES_DIR, managedObject, EcimToYang.ECIM);
            final String expectedNotificationOut = removeWhiteSpace(getXmlFor(TEST_FILES_DIR, managedObject, EcimToYang.YANG));
            assertEquals(expectedNotificationOut, removeWhiteSpace(transformer.transform(ecimNotificationIn)));
        } catch (final TransformerException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

}
