/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.io;

import net.covers1624.quack.net.download.DownloadListener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} implementation which will report Progress
 * to an {@link DownloadListener}.
 * <p>
 * Created by covers1624 on 22/11/21.
 */
public class ProgressInputStream extends FilterInputStream {

    private final DownloadListener listener;
    private long totalLen;

    public ProgressInputStream(InputStream in, DownloadListener listener) {
        this(in, listener, 0);
    }

    public ProgressInputStream(InputStream in, DownloadListener listener, long totalLen) {
        super(in);
        this.listener = listener;
        this.totalLen = totalLen;
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r == -1) {
            listener.finish(totalLen);
        } else {
            totalLen++;
            listener.update(totalLen);
        }
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        totalLen += n;
        listener.update(totalLen);
        return super.skip(n);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
        if (r == -1) {
            listener.finish(totalLen);
        } else {
            totalLen += r;
            listener.update(totalLen);
        }
        return r;
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
