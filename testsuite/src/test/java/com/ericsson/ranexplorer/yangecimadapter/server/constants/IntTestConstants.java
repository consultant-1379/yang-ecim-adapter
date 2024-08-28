/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */
package com.ericsson.ranexplorer.yangecimadapter.server.constants;

public class IntTestConstants{

    public static final int VERIFY_TIMEOUT = 15;
    public static final int SESSION_TIMEOUT = 60;

    public static final String ADAPTER = "Adapter";
    public static final String NETSIM = "Netsim";
    public static final String REALNODE = "RealNode";

    public static final String OK_XML_TAG = "<ok/>";
    public static final String ONE_DOT_ZERO_TERMINATION = "]]>]]>";
    public static final String ONE_DOT_ONE_TERMINATION = "\n##\n";

    public static final String RPC_WRAPPER = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "message-id=\"%s\">%s</rpc>";

    private IntTestConstants() { }
}
