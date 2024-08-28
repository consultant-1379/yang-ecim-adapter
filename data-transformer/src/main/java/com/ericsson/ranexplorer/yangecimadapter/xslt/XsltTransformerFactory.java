/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

public class XsltTransformerFactory {

    private XsltTransformerFactory() {
    }

    public static XsltTransformer newYangToEcimTransformer(final String managedElementId, final String templateType) {
        return new XsltTransformer("enodeb-yang-to-ecim.xsl", managedElementId, templateType);
    }

    public static XsltTransformer newEcimToYangTransformer() {
        return new XsltTransformer("enodeb-ecim-to-yang.xsl");
    }

    public static XsltTransformer newMergeResultsDefaultFilterTransformer() {
        return new XsltTransformer("merge-default-filter-results.xsl");
    }

    public static XsltTransformer newNotificationTransformer() {
        return new XsltTransformer("enodeb-notification.xsl");
    }
}
