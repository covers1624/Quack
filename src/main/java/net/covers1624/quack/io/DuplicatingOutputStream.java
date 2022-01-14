/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * An {@link OutputStream} which duplicates all data provided
 * into every provided {@link OutputStream}.
 * <p>
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
    public void write(byte[] b) throws IOException {
        for (OutputStream s : sinks) {
            s.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream s : sinks) {
            s.write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream s : sinks) {
            s.close();
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream s : sinks) {
            s.flush();
        }
    }
}
