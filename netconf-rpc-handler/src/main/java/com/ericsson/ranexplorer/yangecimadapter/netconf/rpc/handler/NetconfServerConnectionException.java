package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler;

public class NetconfServerConnectionException extends RuntimeException{
        public NetconfServerConnectionException(String message) {
            super(message);
        }

        public NetconfServerConnectionException(String message, Throwable e) {
            super(message, e);
        }

        public NetconfServerConnectionException(Throwable e) {
            super(e);
        }

}
