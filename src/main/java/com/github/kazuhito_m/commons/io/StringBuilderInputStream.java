package com.github.kazuhito_m.commons.io;

import java.io.InputStream;

/**
 * StringBuilderを入力元とするInputStreamクラス。<br>
 *
 * @author Kazuhito Miura
 */
public class StringBuilderInputStream extends InputStream {

    protected String buffer;

    protected int pos;

    protected int count;

    public StringBuilderInputStream(StringBuilder sb) {
        this.buffer = sb.toString();
        count = sb.length();
    }

    @Override
    public int read() {
        return (pos < count) ? (buffer.charAt(pos++) & 0xFF) : -1;
    }

    @Override
    public int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= count) {
            return -1;
        }
        if (pos + len > count) {
            len = count - pos;
        }
        if (len <= 0) {
            return 0;
        }
        String s = buffer.toString();
        int cnt = len;
        while (--cnt >= 0) {
            b[off++] = (byte) s.charAt(pos++);
        }

        return len;
    }

    @Override
    public long skip(long n) {
        if (n < 0) {
            return 0;
        }
        if (n > count - pos) {
            n = count - pos;
        }
        pos += n;
        return n;
    }

    @Override
    public int available() {
        return count - pos;
    }

    @Override
    public void reset() {
        pos = 0;
    }
}
