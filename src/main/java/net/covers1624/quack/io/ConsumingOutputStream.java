/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.covers1624.quack.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * A simple OutputStream treating all input as Text,
 * delegating each line of text to the provided consumer.
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
    public void write(int b) throws IOException {
        char ch = (char) (b & 0xFF);
        buffer.append(ch);
        if (ch == '\n' || ch == '\r') {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        if (buffer.length() == 0) {
            return;
        }
        char end = buffer.charAt(buffer.length() - 1);
        if (end == '\n' || end == '\r') {
            consumer.accept(buffer.toString().trim());
            buffer.setLength(0);
        }
    }
}
