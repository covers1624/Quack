/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import net.covers1624.quack.io.ByteArrayReadableChannel;
import net.covers1624.quack.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a request or response body.
 * <p>
 * Created by covers1624 on 1/8/23.
 */
public interface WebBody {

    /**
     * Open a stream to read the body data.
     *
     * @return The stream.
     * @throws IOException If an IO error occurs.
     */
    InputStream open() throws IOException;

    /**
     * Open a byte channel into a body data.
     * <p>
     * If the underlying data does not support Channels,
     * the {@link #open()} {@link InputStream} will be wrapped
     * to a {@link ReadableByteChannel}.
     *
     * @return The channel.
     * @throws IOException If an IO error occurs.
     */
    default ReadableByteChannel openChannel() throws IOException {
        return Channels.newChannel(open());
    }

    /**
     * Checks if this WebBody supports {@link #open()} being
     * called multiple times.
     *
     * @return If the body can be opened multiple times.
     */
    boolean multiOpenAllowed();

    /**
     * The length of the data.
     *
     * @return The length in bytes. {@code -1} for unknown.
     */
    long length();

    /**
     * The mime content type for this data.
     *
     * @return The content type.
     */
    @Nullable
    String contentType();

    /**
     * Read the body as a byte array.
     *
     * @return The bytes.
     */
    default byte[] asBytes() throws IOException {
        try (InputStream is = open()) {
            return IOUtils.toBytes(is);
        }
    }

    /**
     * Reads the body as a UTF-8 String.
     *
     * @return The String.
     */
    default String asString() throws IOException {
        return asString(StandardCharsets.UTF_8);
    }

    /**
     * Read the body as a String.
     *
     * @return The String.
     */
    default String asString(Charset charset) throws IOException {
        return new String(asBytes(), charset);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@code null} {@code contentType}
     * This method assumes {@link StandardCharsets#UTF_8} {@code charset}.
     *
     * @param str The string.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str) {
        return string(str, (String) null);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@link StandardCharsets#UTF_8} {@code charset}.
     *
     * @param str         The string.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, @Nullable String contentType) {
        return string(str, StandardCharsets.UTF_8, contentType);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     * This method assumes {@code null} {@code Content-Type}
     *
     * @param str     The string.
     * @param charset The {@link Charset} for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, Charset charset) {
        return string(str, charset, null);
    }

    /**
     * Create a {@link WebBody} from a {@link String}.
     *
     * @param str         The string.
     * @param charset     The {@link Charset} for the body.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}.
     */
    static WebBody string(String str, Charset charset, @Nullable String contentType) {
        return bytes(str.getBytes(charset), contentType);
    }

    /**
     * Create a {@link WebBody} form a {@code byte[]}.
     * This method assumes {@code null} {@code Content-Type}
     *
     * @param bytes The bytes.
     * @return The {@link WebBody}
     */
    static WebBody bytes(byte[] bytes) {
        return new BytesBody(bytes, null);
    }

    /**
     * Create a {@link WebBody} form a {@code byte[]}.
     *
     * @param bytes       The bytes.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}
     */
    static WebBody bytes(byte[] bytes, @Nullable String contentType) {
        return new BytesBody(bytes, contentType);
    }

    /**
     * Create a {@link WebBody} for a {@link Path}.
     * This method assumes {@code null} {@code Content-Type}
     *
     * @param path The path.
     * @return The {@link WebBody}
     */
    static WebBody path(Path path) {
        return new PathBody(path, null);
    }

    /**
     * Create a {@link WebBody} for a {@link Path}.
     *
     * @param path        The path.
     * @param contentType The {@code Content-Type}  for the body.
     * @return The {@link WebBody}
     */
    static WebBody path(Path path, @Nullable String contentType) {
        return new PathBody(path, contentType);
    }

    /**
     * Simple {@link WebBody} implementation reading from
     * an in-memory, {@code byte[]}.
     */
    class BytesBody implements WebBody {

        private final byte[] bytes;

        @Nullable
        private final String contentType;

        public BytesBody(byte[] bytes, @Nullable String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }

        // @formatter:off
        @Override public InputStream open() { return new ByteArrayInputStream(bytes); }
        @Override public ReadableByteChannel openChannel() { return new ByteArrayReadableChannel(bytes); }
        @Override public boolean multiOpenAllowed() { return true; }
        @Override public long length() { return bytes.length; }
        @Nullable @Override public String contentType() { return contentType; }
        // @formatter:on
    }

    class PathBody implements WebBody {

        public final Path path;
        private final @Nullable String contentType;

        public PathBody(Path path, @Nullable String contentType) {
            this.path = path;
            this.contentType = contentType;
        }

        // @formatter:off
        @Override public InputStream open() throws IOException { return Files.newInputStream(path); }
        @Override public ReadableByteChannel openChannel() throws IOException { return Files.newByteChannel(path); }
        @Override public boolean multiOpenAllowed() { return true; }
        @Override
        public long length() {
            try {
                return Files.size(path);
            } catch (IOException ex) {
                return -1;
            }
        }
        @Override public @Nullable String contentType() { return contentType; }
        // @formatter:on
    }
}
