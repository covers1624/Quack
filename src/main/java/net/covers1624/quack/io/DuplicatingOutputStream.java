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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by covers1624 on 8/8/19.
 */
public class DuplicatingOutputStream extends OutputStream {

    private final OutputStream[] sinks;

    public DuplicatingOutputStream(OutputStream... sinks) {
        this.sinks = Arrays.copyOf(sinks, sinks.length);
    }

    public DuplicatingOutputStream(Collection<OutputStream> sinks) {
        this.sinks = sinks.toArray(new OutputStream[0]);
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream s : sinks) {
            s.write(b);
        }
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        for (OutputStream s : sinks) {
            s.write(b);
        }
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        for (OutputStream s : sinks) {
            s.write(b, off, len);
        }
    }
}
