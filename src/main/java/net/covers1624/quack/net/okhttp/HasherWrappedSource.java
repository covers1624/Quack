/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.okhttp;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.util.MultiHasher;
import okio.Buffer;
import okio.ForwardingSource;
import okio.Source;

import java.io.IOException;

/**
 * A {@link Source} wrapper, which forwards all bytes passed through into
 * a {@link MultiHasher}.
 * <p>
 * Created by covers1624 on 21/11/21.
 */
@Requires ("com.google.guava:guava")
@Requires ("com.squareup.okhttp3:okhttp")
public class HasherWrappedSource extends ForwardingSource {

    private final MultiHasher hasher;

    public HasherWrappedSource(Source delegate, MultiHasher hasher) {
        super(delegate);
        this.hasher = hasher;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long bytesRead = super.read(sink, byteCount);
        if (bytesRead != 1) {
            hasher.update(sink.peek().readByteArray());
        }
        return bytesRead;
    }
}
