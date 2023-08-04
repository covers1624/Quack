/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        @Override public boolean multiOpenAllowed() { return true; }
        @Override public long length() { return bytes.length; }
        @Nullable @Override public String contentType() { return contentType; }
        // @formatter:on
    }

    class PathBody implements WebBody {

        private final Path path;
        private final @Nullable String contentType;

        public PathBody(Path path, @Nullable String contentType) {
            this.path = path;
            this.contentType = contentType;
        }

        // @formatter:off
        @Override public InputStream open() throws IOException { return Files.newInputStream(path); }
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
