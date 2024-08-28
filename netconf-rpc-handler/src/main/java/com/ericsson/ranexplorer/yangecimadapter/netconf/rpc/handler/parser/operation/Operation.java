/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.xml.sax.Attributes;

import java.io.PrintWriter;

public interface Operation {

    Operation processTag(String tag, Attributes attributes, int level);

    Operation processEndTag(String tag, int level);

    Operation characters(char[] ch, int start, int length);

    Operation invoke(CommandListener commandListener, PrintWriter out);

    Operation sendError(CommandListener commandListener, String errorMessage, PrintWriter out);

}
