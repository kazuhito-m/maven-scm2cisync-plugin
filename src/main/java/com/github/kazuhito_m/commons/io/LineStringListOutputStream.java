package com.github.kazuhito_m.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 流れてきたバイト列をList<String>型のオブジェクトへと出力するストリーム。
 *
 * @author Kazuhito Miura
 */
public class LineStringListOutputStream extends OutputStream {

    /**
     * 一行分を蓄積するStringBuilder
     */
    private StringBuilder line = new StringBuilder();

    /**
     * 結果を出力するListオブジェクト
     */
    private List<String> lines;

    /**
     * コンストラクタ
     */
    public LineStringListOutputStream(List<String> stringList) {
        this.lines = stringList;
    }

    /**
     * Buffers and calls line lines when a line is complete
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        if (c == '\n') {
            lines.add(line.toString());
            line.setLength(0);
        } else if (c != '\r') {
            line.append(c);
        }
    }
}
