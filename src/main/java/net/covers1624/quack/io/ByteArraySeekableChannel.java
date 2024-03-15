/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * A simple {@link SeekableByteChannel} implementation, similar to
 * {@link ByteArrayOutputStream} which manages a growable buffer.
 * <p>
 * Created by covers1624 on 14/3/24.
 */
public class ByteArraySeekableChannel implements SeekableByteChannel {

    private byte[] buf;
    private int pos;
    private int last;

    private boolean open = true;

    public ByteArraySeekableChannel() {
        this(32);
    }

    public ByteArraySeekableChannel(int size) {
        if (size < 0) throw new IllegalArgumentException("Negative initial size: " + size);
        buf = new byte[size];
    }

    @Override
    public int read(ByteBuffer dst) {
        if (pos == buf.length) return -1;

        int toRead = Math.min(buf.length - pos, dst.remaining());
        dst.put(buf, pos, toRead);
        pos += toRead;
        return toRead;
    }

    @Override
    public int write(ByteBuffer src) {
        int toRead = src.remaining();
        fitBuffer(pos + toRead);
        src.get(buf, pos, toRead);
        pos += toRead;
        last = Math.max(last, pos);
        return toRead;
    }

    @Override
    public long position() {
        return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        pos = (int) newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return last;
    }

    private void fitBuffer(int desiredCap) {
        // Enough space.
        if (desiredCap - buf.length <= 0) return;

        int oldCap = buf.length;
        int newCap = oldCap << 1;
        if (newCap - desiredCap < 0) {
            newCap = desiredCap; // If double the buffer is not enough, do exact.
        }
        buf = Arrays.copyOf(buf, newCap);
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }
}
