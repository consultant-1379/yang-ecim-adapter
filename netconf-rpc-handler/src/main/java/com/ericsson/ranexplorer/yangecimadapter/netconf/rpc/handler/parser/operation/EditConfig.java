/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import java.io.PrintWriter;

import com.ericsson.oss.mediation.util.netconf.api.editconfig.DefaultOperation;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.ErrorOption;
import com.ericsson.oss.mediation.util.netconf.api.editconfig.TestOption;
import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import com.ericsson.oss.mediation.util.netconf.api.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * 
 * @author ebialan
 */
public class EditConfig extends Rpc {

    private static final Logger logger = LoggerFactory.getLogger(EditConfig.class);

    private boolean isTarget;
    private boolean isDefaultOperation;
    private boolean isErrorOption;
    private boolean isTestOption;
    private String defaultOperationValue;
    private String errorOptionValue;
    private String testOptionValue;
    private DefaultOperation defaultOperation;
    private ErrorOption errorOption;
    private TestOption testOption;
    private Datastore datastore;
    private String rpcError = "";
    private final StringBuilder requestBody = new StringBuilder();

    protected EditConfig(final Rpc rpc) {
        super(rpc);
        this.isTarget = false;
        this.isDefaultOperation = false;
        this.isErrorOption = false;
        this.isTestOption = false;
        this.defaultOperationValue = "";
        this.errorOptionValue = "";
        this.testOptionValue = "";
        this.defaultOperation = DefaultOperation.NOT_REQUESTED;
        this.errorOption = ErrorOption.NOT_REQUESTED;
        this.testOption = TestOption.NOT_REQUESTED;
        this.datastore = Datastore.RUNNING;
    }

    @Override
    public EditConfig processTag(final String tag, final Attributes attributes, final int level) {
        if (level == 2 && "target".equalsIgnoreCase(tag)) {
            isTarget = true;
            logger.trace("1) Level [{}], tag [{}]", level, tag);
        } else if (level == 2 && "default-operation".equalsIgnoreCase(tag)) {
            isDefaultOperation = true;
            logger.trace("2) Level [{}], tag [{}]", level, tag);
        } else if (level == 2 && "error-option".equalsIgnoreCase(tag)) {
            isErrorOption = true;
            logger.trace("3) Level [{}], tag [{}]", level, tag);
        } else if (level == 2 && "test-option".equalsIgnoreCase(tag)) {
            isTestOption = true;
            logger.trace("4) Level [{}], tag [{}]", level, tag);
        } else if (level == 2 && "config".equalsIgnoreCase(tag)) {
        } else if (level == 3 && isTarget) {
            logger.trace("5) Level [{}], tag [{}]", level, tag);
            try {
                datastore = Datastore.valueOf(tag.toUpperCase());
            } catch (final IllegalArgumentException e) {
                logger.warn("Unknown target for datastore: {}", tag);
                rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
            }
        }else if(level >= 3){
            logger.trace("6) Level [{}], tag [{}]", level, tag);
            appendTagAttributes(attributes, tag);
        }
        return this;
    }

    private void appendTagAttributes(final Attributes attributes, final String tag) {
        requestBody.append("<").append(tag);
        for (int i = 0; i < attributes.getLength(); i++) {
            logger.trace("attribute: qName [{}] localName[{}] value [{}]",
                    attributes.getQName(i), attributes.getLocalName(i), attributes.getValue(i));
            if (!attributes.getQName(i).contains("xmlns")) {
                requestBody.append(" ").append(attributes.getLocalName(i)).append("=\"")
                        .append(attributes.getValue(i)).append("\"");
            }
        }
        requestBody.append(">");
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        final String characters = new StringBuffer().append(ch, start, length).toString();
        logger.trace("Characters: [{}]", characters);

        if (isDefaultOperation) {
            defaultOperationValue = characters;
            try {
                defaultOperation = DefaultOperation.valueOf(defaultOperationValue.replaceAll("-", "_").trim()
                        .toUpperCase());
            } catch (final IllegalArgumentException e) {
                logger.warn("Exception: {} \n Unknown default operation: {}", e.getMessage(), defaultOperationValue, e);
                rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
            }
        } else if (isErrorOption) {
            errorOptionValue = characters;
            try {
                errorOption = ErrorOption.valueOf(errorOptionValue.replaceAll("-", "_").trim().toUpperCase());
            } catch (final IllegalArgumentException e) {
                logger.warn("Exception: {} \n Unknown error option: {}", e.getMessage(), errorOptionValue, e);
                rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
            }
        } else if (isTestOption) {
            testOptionValue = characters;
            try {
                testOption = TestOption.valueOf(testOptionValue.replaceAll("-", "_").trim().toUpperCase());
            } catch (final IllegalArgumentException e) {
                logger.warn("Exception: {} \n Unknown test option: {}", e.getMessage(), testOptionValue.replaceAll("-", "_").trim().toUpperCase(), e);
                rpcError = String.format(RpcReplyFormat.RPC_ERROR_EDIT_CONFIG_BAD_OPERATION, messageId);
            }
        } else if (requestBody != null) {
            requestBody.append(characters);
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        if (level == 2 && "target".equals(tag.toLowerCase())) {
            isTarget = false;
            logger.trace("1) Level [{}], tag [{}]", level, tag);
        } else if (level == 2 && "default-operation".equals(tag.toLowerCase())) {
            logger.trace("2) Level [{}], tag [{}]", level, tag);
            isDefaultOperation = false;
        } else if (level == 2 && "error-option".equals(tag.toLowerCase())) {
            logger.trace("3) Level [{}], tag [{}]", level, tag);
            isErrorOption = false;
        } else if (level == 2 && "test-option".equals(tag.toLowerCase())) {
            logger.trace("4) Level [{}], tag [{}]", level, tag);
            isTestOption = false;
        } else if (level == 2 && "config".equals(tag.toLowerCase())) {
        } else if (level == 3 && isTarget) {
        } else if (level > 2 && requestBody != null) {
            requestBody.append("</").append(tag).append(">");
            logger.trace("5) Level [{}], tag [{}]", level, tag);
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        if (rpcError.isEmpty()) {
            logger.debug("default-operation: {}", defaultOperationValue);
            logger.debug("error-option: {}", errorOptionValue);
            logger.debug("test-option: {}", testOptionValue);
            logger.debug("request body: {} ", requestBody);
            commandListener.editConfig(messageId, datastore, defaultOperation, errorOption, testOption,
                    requestBody.toString(), out);
        } else {
            commandListener.sendError(messageId, rpcError, out);
        }
        return new Nop();
    }
}
