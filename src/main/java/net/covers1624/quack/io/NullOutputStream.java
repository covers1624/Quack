/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation, that does literally nothing
 * with any data piped in. This class is a singleton, {@link #INSTANCE}.
 * <p>
 * Created by covers1624 on 19/11/20.
 */
public class NullOutputStream extends OutputStream {

    public static final NullOutputStream INSTANCE = new NullOutputStream();

    //@formatter:off
    @Override public void write(int b) { }
    @Override public void write(byte[] b) { }
    @Override public void write(byte[] b, int off, int len) { }
    @Override public void close() { }
    @Override public void flush() { }
    //@formatter:on

}
