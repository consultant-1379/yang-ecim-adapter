/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */
package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

/**
 * arg0: Attribute arg1: Element
 */
public class RpcReplyFormat {

    /**
     * arg0: MessageId
     */
    public static final String RPC_OK = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator()
	    + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator() + "<ok/>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * Missing Message-id
     */
    public static final String RPC_ERROR_MISSING_MESSGAE_ID = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
	    + System.lineSeparator()
	    + "<rpc-error>"
	    + System.lineSeparator()
	    + "  <error-type>rpc</error-type>"
	    + System.lineSeparator()
	    + "  <error-tag>missing-attribute</error-tag>"
	    + System.lineSeparator()
	    + "  <error-severity>error</error-severity>"
	    + System.lineSeparator()
	    + "  <error-info>"
	    + System.lineSeparator()
	    + "    <bad-attribute>message-id</bad-attribute>"
	    + System.lineSeparator()
	    + "    <bad-element>rpc</bad-element>"
	    + System.lineSeparator()
	    + "  </error-info>"
	    + System.lineSeparator() + "</rpc-error>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId, arg1: Element, arg2: Attribute
     */
    public static final String RPC_ERROR_UNKNOWN_ATTRIBUTE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	    + System.lineSeparator()
	    + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator() + "<rpc-error>" + System.lineSeparator()
	    + "    <error-type>application</error-type>" + System.lineSeparator()
	    + "    <error-tag>operation-failed</error-tag>" + System.lineSeparator()
	    + "    <error-severity>error</error-severity>" + System.lineSeparator()
	    + "    <error-message xml:lang=\"en\">Error info:\n " + "{unknown_attribute,\"%s\",'%s'}"
	    + System.lineSeparator() + "    </error-message>" + System.lineSeparator() + "</rpc-error>"
	    + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId, arg1: error message
     */
    public static final String RPC_ERROR_FAILED_TO_PARSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	    + System.lineSeparator()
	    + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator() + "  <rpc-error>" + System.lineSeparator()
	    + "    <error-type>application</error-type>" + System.lineSeparator()
	    + "    <error-tag>operation-failed</error-tag>" + System.lineSeparator()
	    + "    <error-severity>error</error-severity>" + System.lineSeparator() + "    <error-message>"
	    + System.lineSeparator() + "      Unexpected error: {failed_to_parse_xml," + System.lineSeparator()
	    + "      {fatal, {%s}}}," + System.lineSeparator() + "    </error-message>" + System.lineSeparator()
	    + "  </rpc-error>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId, arg1: element, arg2: error message
     */
    public final static String RPC_ERROR_MISSING_ELEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	    + System.lineSeparator()
	    + "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator() + "<rpc-error>" + System.lineSeparator()
	    + "    <error-type>application</error-type>" + System.lineSeparator()
	    + "    <error-tag>missing-element</error-tag>" + System.lineSeparator()
	    + "    <error-severity>error</error-severity>" + System.lineSeparator() + "    <error-info>"
	    + System.lineSeparator() + "        <missing-element>%s</missing-element>" + System.lineSeparator()
	    + "    </error-info>" + System.lineSeparator() + "    <error-message xml:lang=\"en\">%s</error-message>"
	    + System.lineSeparator() + "</rpc-error>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId
     */
    public final static String RPC_ERROR_EDIT_CONFIG_BAD_OPERATION = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator()
	    + "<rpc-error>"
	    + System.lineSeparator()
	    + "    <error-type>protocol</error-type>"
	    + System.lineSeparator()
	    + "    <error-tag>operation-not-supported</error-tag>"
	    + System.lineSeparator()
	    + "    <error-severity>error</error-severity>"
	    + System.lineSeparator()
	    + "    <error-message xml:lang=\"en\">Failed to build operation</error-message>"
	    + System.lineSeparator()
	    + "</rpc-error>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId
     */
    public final static String RPC_ERROR_CREATE_SUBSCRIPTION_FAILED = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator()
	    + "<rpc-error>"
	    + System.lineSeparator()
	    + "    <error-type>protocol</error-type>"
	    + System.lineSeparator()
	    + "    <error-tag>operation-failed</error-tag>"
	    + System.lineSeparator()
	    + "    <error-severity>error</error-severity>"
	    + System.lineSeparator()
	    + "    <error-message xml:lang=\"en\">Request could not be completed because the requested operation failed for some reason not covered by any other error condition.</error-message>"
	    + System.lineSeparator() + "</rpc-error>" + System.lineSeparator() + "</rpc-reply>";

    /**
     * arg0: MessageId arg1: SessionId
     */
    public final static String RPC_ERROR_LOCK_FAILED = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator()
	    + "<rpc-error>"
	    + System.lineSeparator()
	    + "    <error-type>protocol</error-type>"
	    + System.lineSeparator()
	    + "    <error-tag>lock-denied</error-tag>"
	    + System.lineSeparator()
	    + "    <error-severity>error</error-severity>"
	    + System.lineSeparator()
	    + "    <error-message xml:lang=\"en\"> Lock failed, lock is already held</error-message>"
	    + System.lineSeparator()
	    + "<error-info>"
	    + System.lineSeparator()
	    + "<session-id>%s</session-id>"
	    + System.lineSeparator()
	    + "</error-info>"
	    + System.lineSeparator()
	    + "</rpc-error>"
	    + System.lineSeparator() + "</rpc-reply>";

    public final static String RPC_ERROR_UNLOCK_FAILED = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"%s\">"
	    + System.lineSeparator()
	    + "<rpc-error>"
	    + System.lineSeparator()
	    + "    <error-type>protocol</error-type>"
	    + System.lineSeparator()
	    + "    <error-tag>unlock-denied</error-tag>"
	    + System.lineSeparator()
	    + "    <error-severity>error</error-severity>"
	    + System.lineSeparator()
	    + "    <error-message xml:lang=\"en\"> Unlock failed, lock is not held</error-message>"
	    + System.lineSeparator() + "</rpc-error>" + System.lineSeparator() + "</rpc-reply>";

}
