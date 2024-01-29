/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.net.httpapi;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Multipart body.
 * <p>
 * This requires special handling by the underlying http implementation.
 * <p>
 * Created by covers1624 on 1/8/23.
 */
public class MultipartBody implements WebBody {

    private final List<Part> parts;

    public MultipartBody(List<Part> parts) {
        this.parts = new ArrayList<>(parts);
    }

    /**
     * Gets the parts which make up this body.
     *
     * @return The parts.
     */
    public List<Part> getParts() {
        return Collections.unmodifiableList(parts);
    }

    // @formatter:off
    @Override public InputStream open() { throw new UnsupportedOperationException(); }
    @Override public ReadableByteChannel openChannel() { throw new UnsupportedOperationException(); }
    @Override public boolean multiOpenAllowed() { throw new UnsupportedOperationException(); }
    @Override public long length() { throw new UnsupportedOperationException(); }
    @Override public @Nullable String contentType() { throw new UnsupportedOperationException(); }
    // @formatter:on

    public static class Part {

        public final String name;
        public final @Nullable String fileName;
        public final WebBody body;

        public Part(String name, @Nullable String fileName, WebBody body) {
            this.name = name;
            this.fileName = fileName;
            this.body = body;
        }
    }

    public static class Builder {

        private final List<Part> parts = new ArrayList<>();

        /**
         * Add a Multipart body part.
         *
         * @param name The name.
         * @param body The body.
         * @return The same builder.
         */
        public Builder addPart(String name, WebBody body) {
            return addPart(name, null, body);
        }

        /**
         * Add a Multipart body part.
         *
         * @param name     The name.
         * @param fileName The filename.
         * @param body     The body.
         * @return The same builder.
         */
        public Builder addPart(String name, @Nullable String fileName, WebBody body) {
            parts.add(new Part(name, fileName, body));
            return this;
        }

        /**
         * @return The built body.
         */
        public MultipartBody build() {
            return new MultipartBody(parts);
        }
    }
}
