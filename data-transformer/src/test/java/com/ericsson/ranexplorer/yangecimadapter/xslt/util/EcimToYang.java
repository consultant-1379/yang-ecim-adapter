/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt.util;

public enum EcimToYang implements SchemaType {

    YANG("out", "_yang.xml"), ECIM("in", "_ecim.xml");

    private String dirName;
    private String extension;

    private EcimToYang(final String dirName, final String extension) {
        this.dirName = dirName;
        this.extension = extension;
    }

    @Override
    public String getDirName() {
        return dirName;
    }

    @Override
    public String getExtension() {
        return extension;
    }

}
