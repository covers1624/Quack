/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;

import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by covers1624 on 22/1/21.
 */
@Requires ("com.google.guava:guava")
@SuppressWarnings ("UnstableApiUsage")
public class HashUtils {

    /**
     * Adds Creates a {@link HashCode} from the content of the given {@link Path} with the given {@link HashFunction}.
     *
     * @param func The {@link HashFunction} to use.
     * @param path The {@link Path} to read from.
     * @return The {@link HashCode}.
     * @throws IOException If an IO error occurred whilst reading from or opening the {@link Path}
     */
    public static HashCode hash(HashFunction func, Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return hash(func, is);
        }
    }

    /**
     * Adds Creates a {@link HashCode} from the given {@link InputStream} with the given {@link HashFunction}.
     *
     * @param func The {@link HashFunction} to use.
     * @param is   The {@link InputStream} to read from.
     * @return The {@link HashCode}.
     * @throws IOException If an IO error occurred whilst reading the {@link InputStream}.
     */
    public static HashCode hash(HashFunction func, @WillNotClose InputStream is) throws IOException {
        Hasher hasher = func.newHasher();
        addToHasher(hasher, is);
        return hasher.hash();
    }

    /**
     * Copies the content of the provided {@link InputStream} to the provided {@link Hasher}.
     *
     * @param hasher The hasher.
     * @param is     The InputStream.
     * @throws IOException If something is bork.
     */
    public static void addToHasher(Hasher hasher, @WillNotClose InputStream is) throws IOException {
        byte[] buffer = IOUtils.getCachedBuffer();
        int len;
        while ((len = is.read(buffer)) != -1) {
            hasher.putBytes(buffer, 0, len);
        }
    }

    /**
     * Checks if the provided {@link HashCode} equals the provided hash String.
     *
     * @param a The first hash.
     * @param b The second hash.
     */
    public static boolean equals(@Nullable HashCode a, @Nullable String b) {
        if (a == null || b == null || b.isEmpty()) {
            return false;
        }
        try {
            return a.equals(HashCode.fromString(b));
        } catch (Throwable ignored) {
            return false;
        }
    }

}
