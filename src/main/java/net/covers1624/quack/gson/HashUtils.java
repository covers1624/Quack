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

package net.covers1624.quack.gson;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;

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
    public static boolean equals(HashCode a, String b) {
        if (a == null || b == null) {
            return false;
        }
        try {
            return a.equals(HashCode.fromString(b));
        } catch (Throwable ignored) {
            return false;
        }
    }

}
