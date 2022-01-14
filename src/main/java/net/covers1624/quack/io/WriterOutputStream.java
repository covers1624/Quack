/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * A simple {@link OutputStream} to output to a {@link Writer}.
 * <p>
 * Created by covers1624 on 20/11/21.
 */
public class WriterOutputStream extends OutputStream {

    // Decoder buffers.
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(128);
    private final CharBuffer charBuffer = CharBuffer.allocate(1024);

    private final Writer writer;
    private final CharsetDecoder decoder;
    private final boolean autoFlush;

    public WriterOutputStream(Writer writer) {
        this(writer, StandardCharsets.UTF_8);
    }

    public WriterOutputStream(Writer writer, Charset charset) {
        this(writer, charset, true);
    }

    public WriterOutputStream(Writer writer, boolean autoFlush) {
        this(writer, StandardCharsets.UTF_8, autoFlush);
    }

    public WriterOutputStream(Writer writer, Charset charset, boolean autoFlush) {
        this(writer, charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?"), autoFlush);
    }

    public WriterOutputStream(Writer writer, CharsetDecoder decoder, boolean autoFlush) {
        this.writer = writer;
        this.decoder = decoder;
        this.autoFlush = autoFlush;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int min = Math.min(len, byteBuffer.remaining());
            byteBuffer.put(b, off, min);
            handleBytes(true);
            len -= min;
            off += min;
        }
        if (autoFlush) {
            flushInternal();
        }
    }

    @Override
    public void flush() throws IOException {
        flushInternal();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        handleBytes(false);
        flushInternal();
        writer.close();
    }

    private void handleBytes(boolean hasMore) throws IOException {
        byteBuffer.flip();
        while (true) {
            CoderResult result = decoder.decode(byteBuffer, charBuffer, !hasMore);
            if (result.isOverflow()) {
                flushInternal();
            } else if (result.isUnderflow()) {
                break;
            } else {
                result.throwException();
            }
        }
        byteBuffer.compact();
    }

    private void flushInternal() throws IOException {
        if (charBuffer.position() <= 0) return;
        writer.write(charBuffer.array(), 0, charBuffer.position());
        charBuffer.rewind();
    }

    //@formatter:off
    @Override public void write(byte[] b) throws IOException { write(b, 0, b.length); }
    @Override public void write(int b) throws IOException { write(new byte[] { (byte) b }, 0, 1); }
    //@formatter:on
}
