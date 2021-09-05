/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
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
        char end = buffer.charAt(buffer.length() - 1);
        if (end == '\n') {
            consumer.accept(buffer.toString().trim());
            buffer.setLength(0);
        }
    }
}
