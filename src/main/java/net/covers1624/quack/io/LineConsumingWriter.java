/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 5/17/26.
 */
public class LineConsumingWriter extends Writer {

    private final Consumer<String> consumer;
    private final StringBuilder buffer = new StringBuilder();

    public LineConsumingWriter(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < len; i++) {
            write(cbuf[i]);
        }
    }

    @Override
    public void write(int c) throws IOException {
        char ch = (char) c;
        buffer.append(ch);
        if (ch == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        if (buffer.length() == 0) {
            return;
        }
        // If the end of the buffer is a newline..
        int endIdx = buffer.length() - 1;
        if (buffer.charAt(endIdx) == '\n') {
            // If there is a trailing carriage return, strip it.
            if (endIdx - 1 >= 0 && buffer.charAt(endIdx - 1) == '\r') {
                endIdx--;
            }
            consumer.accept(buffer.substring(0, endIdx));
            buffer.setLength(0);
        }
    }

    @Override
    public void close() throws IOException { }
}
