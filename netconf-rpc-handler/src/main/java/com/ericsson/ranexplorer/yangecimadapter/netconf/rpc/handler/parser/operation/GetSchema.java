package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.parser.operation;

import com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.CommandListener;
import org.xml.sax.Attributes;

import java.io.PrintWriter;

public class GetSchema  extends Rpc {

    private static final String IDENTIFIER_TAG = "identifier";
    private String identifier;
    private boolean isIdentifier;

    public GetSchema(Rpc rpc) {
        super(rpc);
    }

    @Override
    public GetSchema processTag(final String tag, final Attributes attributes, final int level) {
        if(level == 2 && tag.equalsIgnoreCase(IDENTIFIER_TAG)) {
             isIdentifier = true;
        }
        return this;
    }

    @Override
    public Operation characters(final char[] ch, final int start, final int length) {
        if(isIdentifier){
            identifier = String.valueOf(ch, start, length);
        }
        return this;
    }

    @Override
    public Operation processEndTag(final String tag, final int level) {
        if(level == 2 && tag.equalsIgnoreCase(IDENTIFIER_TAG)) {
                isIdentifier = false;
        }
        return this;
    }

    @Override
    public Operation invoke(final CommandListener commandListener, final PrintWriter out) {
        commandListener.getSchema(messageId, identifier, out);
        return new Nop();
    }
}
