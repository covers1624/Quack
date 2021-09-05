/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.io;

import net.covers1624.quack.annotation.ReplaceWith;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation, that does literally nothing
 * with any data piped in. This class is a singleton, {@link #INSTANCE}.
 * <p>
 * Created by covers1624 on 19/11/20.
 */
public class NullOutputStream extends OutputStream {

    public static final NullOutputStream INSTANCE = new NullOutputStream();

    //Singleton.
    @Deprecated
    @ReplaceWith ("#INSTANCE")
    @ScheduledForRemoval (inVersion = "0.4.0")
    public NullOutputStream() { }

    //@formatter:off
    @Override public void write(int b) { }
    @Override public void write(byte[] b) { }
    @Override public void write(byte[] b, int off, int len) { }
    @Override public void close() { }
    @Override public void flush() { }
    //@formatter:on

}
