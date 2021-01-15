/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.covers1624.quack.io;

import net.covers1624.quack.io.DelegateFileSystem;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 14/1/21.
 */
public class IOUtils {

    //32k buffer.
    private static final ThreadLocal<byte[]> bufferCache = ThreadLocal.withInitial(() -> new byte[32 * 1024]);
    private static final Map<String, String> jfsArgsCreate = Collections.singletonMap("create", "true");

    /**
     * Copies the content of an {@link InputStream} to an {@link OutputStream}.
     *
     * @param is The {@link InputStream}.
     * @param os The {@link OutputStream}.
     * @throws IOException If something is bork.
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = bufferCache.get();
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    /**
     * Reads an {@link InputStream} to a byte array.
     *
     * @param is The InputStream.
     * @return The bytes.
     * @throws IOException If something is bork.
     */
    public static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os);
        return os.toByteArray();
    }

    /**
     * Reads the provided array of bytes into a List of UTF-8 Strings.
     *
     * @param bytes The bytes of the strings.
     * @return The lines.
     * @throws IOException Any exception thrown reading the bytes.
     */
    public static List<String> readAll(byte[] bytes) throws IOException {
        return readAll(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads the provided array of bytes into a List of Strings
     * in the given {@link Charset}.
     *
     * @param bytes The bytes of the strings.
     * @return The lines.
     * @throws IOException Any exception thrown reading the bytes.
     */
    public static List<String> readAll(byte[] bytes, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    public static FileSystem getJarFileSystem(Path path, boolean create) throws IOException {
        return getJarFileSystem(path.toUri(), create);
    }

    public static FileSystem getJarFileSystem(URI path, boolean create) throws IOException {
        URI jarURI;
        try {
            jarURI = new URI("jar:file", null, path.getPath(), "");
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return getFileSystem(jarURI, create ? jfsArgsCreate : Collections.emptyMap());
    }

    public static FileSystem getFileSystem(URI uri) throws IOException {
        return getFileSystem(uri, Collections.emptyMap());
    }

    public static FileSystem getFileSystem(URI uri, Map<String, ?> env) throws IOException {
        FileSystem fs;
        boolean owner = true;
        try {
            fs = FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException e) {
            fs = FileSystems.getFileSystem(uri);
            owner = false;
        }
        return owner ? fs : protectClose(fs);
    }

    /**
     * wraps the given {@link FileSystem} protecting it against {@link FileSystem#close()} operations.
     *
     * @param fs The {@link FileSystem} to wrap.
     * @return The wrapped {@link FileSystem}.
     */
    public static FileSystem protectClose(FileSystem fs) {
        return new DelegateFileSystem(fs) {
            @Override
            public void close() {
            }
        };
    }

    /**
     * wraps the given {@link InputStream} protecting it against {@link InputStream#close()} operations.
     *
     * @param is The {@link InputStream} to wrap.
     * @return The wrapped {@link InputStream}.
     */
    public static InputStream protectClose(InputStream is) {
        return new FilterInputStream(is) {
            @Override
            public void close() {
            }
        };
    }

    /**
     * wraps the given {@link OutputStream} protecting it against {@link OutputStream#close()} operations.
     *
     * @param os The {@link OutputStream} to wrap.
     * @return The wrapped {@link OutputStream}.
     */
    public static OutputStream protectClose(OutputStream os) {
        return new FilterOutputStream(os) {
            @Override
            public void close() {
            }
        };
    }
}
