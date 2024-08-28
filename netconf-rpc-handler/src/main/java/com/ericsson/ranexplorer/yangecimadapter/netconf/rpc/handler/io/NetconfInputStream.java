/*
 * ----------------------------------------------------------------------------
 *     Copyright (c) 2012 - 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 *
 */

package com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NetconfInputStream extends FilterInputStream {

    protected final static byte[] DELIMITER = "]]>]]>".getBytes(StandardCharsets.UTF_8);
    protected final NetconfStreamBuffer buf = new NetconfStreamBuffer(1024);
    protected final boolean preventClose;
    protected boolean eof;
    protected boolean errorReported;

    public NetconfInputStream(final InputStream in, final boolean preventClose) {
        super(in);
        this.preventClose = preventClose;
    }

    public NetconfInputStream(final InputStream in) {
        this(in, false);
    }

    @Override
    public int read() throws IOException {
        int b;
        if (this.errorReported) {
            b = this.buf.readSkippingLeadingSpaces();
            if (b == -1 && !this.eof) {
                b = this.readSkippingLeadingSpaces();
            }
        } else {
            b = this.buf.read();
            if (b == -1 && !this.eof) {
                b = super.read();
            }
        }

        if (b == -1) {
            this.eof = true;
        } else {
            this.errorReported = false;
        }
        return b;
    }

    private int readSkippingLeadingSpaces() throws IOException {
        int b;
        while ((b = super.read()) != -1) {
            if (!Character.isWhitespace(b)) {
                break;
            }
        }
        return b;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        int bRead;
        if (this.errorReported) {
            bRead = this.buf.readSkippingLeadingSpaces(b, off, len);
            if (bRead <= 0 && !this.eof) {
                bRead = this.readSkippingLeadingSpaces(b, off, len);
            }
        } else {
            bRead = this.buf.read(b, off, len);
            if (bRead <= 0 && !this.eof) {
                bRead = super.read(b, off, len);
            }
        }
        if (bRead == -1) {
            this.eof = true;
        }
        int mRead = findDelimiter(b, off, bRead);
        if (mRead == -1) {
            if (bRead < len && !this.eof) {
                final int sRead = super.read(b, off + bRead, len - bRead);
                if (sRead == -1) {
                    this.eof = true;
                } else {
                    bRead += sRead;
                }
                mRead = findDelimiter(b, off, bRead);

            }
        }
        if (mRead != -1) {
            bRead = mRead;
        }
        if (bRead != -1) {
            this.errorReported = false;
        }
        return bRead;
    }

    private int findDelimiter(final byte[] b, final int off, final int len) {
        if (len > 0) {
            int match = findPartialAtStart(b, off, len);
            if (match > -1) {
                this.buf.put(b, match + 1, len - (match - off + 1));
                return match - off + 1;
            } else {
                match = find(b, off, len);
                if (match > -1) {
                    this.buf.put(b, match + DELIMITER.length, len - (match - off + DELIMITER.length));
                    return match - off + DELIMITER.length;
                }
            }
        }
        return -1;
    }

    private int readSkippingLeadingSpaces(final byte[] b, final int off, final int len) throws IOException {
        int bRead = -1;
        while (bRead == -1) {
            bRead = super.read(b, off, len);
            for (int i = off; i < off + len; i++) {
                if (!Character.isWhitespace(b[i])) {
                    System.arraycopy(b, i, b, off, len - i);
                } else {
                    bRead -= 1;
                }
            }
        }
        return bRead;
    }

    private int findPartialAtStart(final byte[] b, final int off, final int len) {
        for (int i = Math.min(DELIMITER.length, len); i > 0; i--) {
            int k = 0;
            for (; k < i; k++) {
                if (b[off + k] != DELIMITER[DELIMITER.length - i + k]) {
                    break;
                }
            }
            if (k == i) {
                return off + i - 1;
            }
        }
        return -1;
    }

    private int find(final byte[] b, final int off, final int read) {
        for (int i = off; i <= off + read - DELIMITER.length; i++) {
            int k = 0;
            for (; k < DELIMITER.length; k++) {
                if (b[i + k] != DELIMITER[k]) {
                    break;
                }
            }
            if (k == DELIMITER.length) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEof() {
        return this.buf.byteBuffer.remaining() == 0 && this.eof;
    }

    public void error() {
        this.errorReported = true;
    }

    @Override
    public void close() throws IOException {
        if (!preventClose) {
            super.close();
        }
    }

}
