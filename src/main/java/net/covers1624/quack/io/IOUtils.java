/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.io;

import javax.annotation.WillNotClose;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Various utilities for IO interaction with bytes, streams, files, etc.
 * <p>
 * Created by covers1624 on 14/1/21.
 */
public class IOUtils {

    //32k buffer.
    private static final ThreadLocal<byte[]> bufferCache = ThreadLocal.withInitial(() -> new byte[32 * 1024]);
    private static final Map<String, String> jfsArgsCreate = Collections.singletonMap("create", "true");

    /**
     * Returns a static per-thread cached 32k buffer for IO operations.
     *
     * @return The buffer.
     */
    public static byte[] getCachedBuffer() {
        return bufferCache.get();
    }

    /**
     * Copies the content of an {@link InputStream} to an {@link OutputStream}.
     *
     * @param is The {@link InputStream}.
     * @param os The {@link OutputStream}.
     * @throws IOException If something is bork.
     */
    public static void copy(@WillNotClose InputStream is, @WillNotClose OutputStream os) throws IOException {
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
    public static byte[] toBytes(@WillNotClose InputStream is) throws IOException {
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

    /**
     * Creates a new {@link FileSystem} for the given jar path.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param path   The file to open the jar for.
     * @param create If the file system should attempt to be created if it does not exist.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getJarFileSystem(Path path, boolean create) throws IOException {
        return getJarFileSystem(path.toUri(), create);
    }

    /**
     * Creates a new {@link FileSystem} for the given uri path.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param path   The uri to open the jar for.
     * @param create If the file system should attempt to be created if it does not exist.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getJarFileSystem(URI path, boolean create) throws IOException {
        URI jarURI;
        try {
            jarURI = new URI("jar:file", null, path.getPath(), "");
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return getFileSystem(jarURI, create ? jfsArgsCreate : Collections.emptyMap());
    }

    /**
     * Attempts to get an already existing {@link FileSystem}.
     * <p>
     * This method will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param uri The uri to open the jar for.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
    public static FileSystem getFileSystem(URI uri) throws IOException {
        return getFileSystem(uri, Collections.emptyMap());
    }

    /**
     * Attempts to get or create a {@link FileSystem} for the given uri, with additional arguments for FS creation.
     * <p>
     * If a FS was not created by this method due to it already existing, this method
     * will guard the returned {@link FileSystem} from being closed via {@link FileSystem#close()}.
     * This means it's safe to use try-with-resources when you don't explicitly own the {@link FileSystem}.
     *
     * @param uri The uri to open the jar for.
     * @param env Any additional arguments to provide when creating the {@link FileSystem}.
     * @return The {@link FileSystem}.
     * @throws IOException If the {@link FileSystem} could not be created.
     */
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

    /**
     * Copes every element in a Jar file from the input to the Jar file output, where every
     * element must match the provided {@link Predicate}.
     *
     * @param input     The Input Path. This should exist.
     * @param output    The Output Path. This should not exists.
     * @param predicate The {@link Predicate} that each Entry must match.
     * @throws IOException If something went wrong during execution.
     */
    public static void stripJar(Path input, Path output, Predicate<Path> predicate) throws IOException {
        if (Files.notExists(input)) throw new FileNotFoundException("Input not found. " + input);
        if (Files.exists(output)) throw new IOException("Output already exists. " + output);

        try (FileSystem inFs = getJarFileSystem(input, true);
             FileSystem outFs = getJarFileSystem(output, true)
        ) {
            Path inRoot = inFs.getPath("/");
            Path outRoot = outFs.getPath("/");
            Files.walkFileTree(inRoot, new CopyingFileVisitor(inRoot, outRoot, predicate));
        }
    }
}
