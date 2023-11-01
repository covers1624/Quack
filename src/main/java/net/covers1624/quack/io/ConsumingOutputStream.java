/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * A simple OutputStream treating all input as Text,
 * delegating each line of text to the provided consumer.
 * <p>
 * This consumer does not support Mac OS classic CR line endings.
 * <p>
 * Created by covers1624 on 1/4/21.
 */
public class ConsumingOutputStream extends OutputStream {

    private final Consumer<String> consumer;
    private final StringBuilder buffer = new StringBuilder();

    public ConsumingOutputStream(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void write(int b) {
        char ch = (char) (b & 0xFF);
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
}
