/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.download.DownloadListener;
import okio.Buffer;
import okio.ForwardingSource;
import okio.Source;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by covers1624 on 21/11/21.
 */
@Requires ("com.squareup.okhttp3:okhttp")
public class ProgressForwardingSource extends ForwardingSource {

    private final DownloadListener listener;
    private long totalLen;

    public ProgressForwardingSource(Source delegate, DownloadListener listener) {
        this(delegate, listener, 0);
    }

    public ProgressForwardingSource(Source delegate, DownloadListener listener, long existingLen) {
        super(delegate);
        this.listener = listener;
        totalLen = existingLen;
    }

    @Override
    public long read(@NotNull Buffer sink, long byteCount) throws IOException {
        long len = super.read(sink, byteCount);
        if (len == -1) {
            listener.finish(totalLen);
        } else {
            totalLen += len;
            listener.update(totalLen);
        }
        return len;
    }
}
