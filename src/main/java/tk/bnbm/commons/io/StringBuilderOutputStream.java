package tk.bnbm.commons.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * StringBuilderを出力先とするOutputStreamクラス。<br>
 *
 * @author Kazuhito Miura
 */
public class StringBuilderOutputStream extends OutputStream {

    private StringBuilder sb;

    private boolean closed = false;

    /**
     * コンストラクタ。
     *
     * @param buffer 出力先となるStringBuilderオブジェクト。
     */
    public StringBuilderOutputStream(StringBuilder buffer) {
        super();
        this.sb = buffer;
    }

    @Override
    public void write(int i) throws IOException {
        if (closed) return;

        sb.append((char) i);
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (closed) return;

        if (b == null)
            throw new NullPointerException("The byte array is null");
        if (offset < 0 || length < 0 || (offset + length) > b.length)
            throw new IndexOutOfBoundsException(
                    "offset and length are negative or extend outside array bounds");

        String str = new String(b, offset, length);
        sb.append(str);
    }

    @Override
    public void close() {
        sb = null;
        closed = true;
    }

}
