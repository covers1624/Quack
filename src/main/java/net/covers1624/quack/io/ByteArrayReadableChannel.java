/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A simple {@link ReadableByteChannel} implementation which reads
 * from a backing array.
 * <p>
 * This is an NIO variant of {@link ByteArrayInputStream}.
 * <p>
 * Created by covers1624 on 11/1/24.
 */
public class ByteArrayReadableChannel implements ReadableByteChannel {

    private final byte[] buf;
    private final int end;
    private int pos;

    private boolean open = true;

    /**
     * Create a new channel from the given buffer.
     * <p>
     * The entire buffer will be read.
     *
     * @param buf The buffer.
     */
    public ByteArrayReadableChannel(byte[] buf) {
        this.buf = buf;
        end = buf.length;
        pos = 0;
    }

    /**
     * Create a new channel from part of the given buffer.
     * <p>
     * The buffer will start reading at {@code pos}
     * and stop reading at {@code end};
     *
     * @param buf The buffer.
     * @param off The start index (inclusive).
     * @param end The end index (exclusive).
     */
    public ByteArrayReadableChannel(byte[] buf, int off, int end) {
        this.buf = buf;
        this.end = end;
        pos = off;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (pos == end) return -1;

        int toRead = Math.min(end - pos, dst.remaining());
        dst.put(buf, pos, toRead);
        pos += toRead;
        return toRead;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        open = false;
    }
}
