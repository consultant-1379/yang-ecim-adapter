/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.filter;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultFilterProviderTest {

    DefaultFilterProvider defaultFilterProvider = DefaultFilterProvider.getInstance();

    @Test
    public void testGetDefaultFilter() {
        final String result = defaultFilterProvider.getDefaultFilter(FilterBlockName.ENODEB_FUNCTION);
        assertTrue(result.contains("<enb-id>"));
        assertTrue("Filter should contain operationalAttribute", result.contains("<operationalAttribute/>"));
    }

    @Test
    public void testGetDefaultFilterConfigDataOnly() {
        final String result = defaultFilterProvider.getDefaultFilterConfigDataOnly(FilterBlockName.ENODEB_FUNCTION);
        assertTrue(result.contains("<enb-id>"));
        assertFalse("Filter should not contain operationalAttribute", result.contains("<operationalAttribute/>"));
    }

}
