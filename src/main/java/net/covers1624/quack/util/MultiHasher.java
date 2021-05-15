/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.IOUtils;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Capable of creating multiple {@link HashCode}s from an input.
 * <p>
 * Created by covers1624 on 15/2/21.
 */
@Requires ("com.google.guava:guava")
@SuppressWarnings ("UnstableApiUsage")
public class MultiHasher {

    private final Map<HashFunc, Hasher> hashers = new HashMap<>();
    private boolean finished;

    public MultiHasher(HashFunc... hashFunctions) {
        this(Arrays.asList(hashFunctions));
    }

    public MultiHasher(Collection<HashFunc> hashFunctions) {
        if (hashFunctions.isEmpty()) {
            throw new IllegalArgumentException("Expected one or more hash functions.");
        }
        for (HashFunc func : hashFunctions) {
            hashers.put(func, func.newHasher());
        }
    }

    /**
     * Finishes this {@link MultiHasher}, returning a {@link HashResult} result.
     * <p>
     * It is invalid to update a {@link MultiHasher} after it has finished.
     *
     * @return The result.
     * @throws IllegalStateException When this {@link MultiHasher} has already been finished.
     */
    public HashResult finish() {
        if (finished) {
            throw new IllegalStateException("MultiHasher already finished.");
        }

        HashMap<HashFunc, HashCode> ret = new HashMap<>();
        for (Map.Entry<HashFunc, Hasher> entry : hashers.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().hash());
        }
        finished = true;
        return new HashResult(ret);
    }

    /**
     * Loads the content of the provided Path into the {@link MultiHasher}.
     *
     * @param path The path to load.
     * @throws IOException If there was an error reading/opening the file.
     */
    public void load(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            load(is);
        }
    }

    /**
     * Loads the content of the given {@link InputStream} into the {@link MultiHasher}.
     * <p>
     * This method will block until there is no more data in the stream.
     *
     * @param is The InputStream to read from.
     * @throws IOException If there was an error reading from the stream
     */
    public void load(@WillNotClose InputStream is) throws IOException {
        byte[] buffer = IOUtils.getCachedBuffer();
        int len;
        while ((len = is.read(buffer)) != -1) {
            update(buffer, 0, len);
        }
    }

    /**
     * Appends the given bytes to the {@link MultiHasher}.
     *
     * @param bytes The Bytes to append.
     */
    public void update(byte[] bytes) {
        update(bytes, 0, bytes.length);
    }

    /**
     * Appends the specified bytes at the offset with the given length to the {@link MultiHasher}.
     *
     * @param bytes  The bytes to add from.
     * @param offset The offset into the buffer.
     * @param len    The length of the bytes to add.
     */
    public void update(byte[] bytes, int offset, int len) {
        for (Hasher hasher : hashers.values()) {
            hasher.putBytes(bytes, offset, len);
        }
    }

    /**
     * Represents a completed result from this {@link MultiHasher}.
     */
    public static class HashResult extends AbstractMap<HashFunc, HashCode> {

        private final Map<HashFunc, HashCode> hashes;

        public HashResult(Map<HashFunc, HashCode> hashes) {
            this.hashes = ImmutableMap.copyOf(hashes);
        }

        /**
         * Returns a Set of Hashes that do not match the provided {@link HashResult}.
         * Any hashes missing from the provided {@link HashResult} will be treated as an error.
         * <p>
         * It is <strong>not</strong> an error for this {@link HashResult} to have
         * missing entries compared to the provided result.
         *
         * @param other The CompletedHashes to compare against.
         * @return The invalid hashes. Empty if everything is fine.
         */
        public Set<HashFunc> findInvalidHashes(HashResult other) {
            Set<HashFunc> invalidHashes = new HashSet<>();
            for (Map.Entry<HashFunc, HashCode> entry : entrySet()) {
                if (!entry.getValue().equals(other.get(entry.getKey()))) {
                    invalidHashes.add(entry.getKey());
                }
            }
            return invalidHashes;
        }

        @Override
        public Set<Entry<HashFunc, HashCode>> entrySet() {
            return hashes.entrySet();
        }
    }

    /**
     * Represents a HashFunction.
     */
    public static final class HashFunc {

        private static final Map<String, HashFunc> HASH_FUNCS = new HashMap<>();

        public static final HashFunc MD5 = create("MD5", Hashing.md5());
        public static final HashFunc SHA1 = create("SHA1", Hashing.sha1());
        public static final HashFunc SHA256 = create("SHA256", Hashing.sha256());
        public static final HashFunc SHA512 = create("SHA512", Hashing.sha512());

        public final String name;
        public final HashFunction hashFunction;

        private HashFunc(String name, HashFunction hashFunction) {
            this.name = Objects.requireNonNull(name);
            this.hashFunction = Objects.requireNonNull(hashFunction);
        }

        /**
         * Creates a new HashFunc storing it in the global map of HashFunctions.
         *
         * @param name         A Unique identifier for this HashFunc, Usually the name of the Algorithm.
         * @param hashFunction The underlying Guava HashFunction.
         * @return The HashFunc handle.
         */
        public static HashFunc create(String name, HashFunction hashFunction) {
            HashFunc func = HASH_FUNCS.get(name);
            if (func != null && func.hashFunction != hashFunction) {
                throw new IllegalArgumentException("Tried to re-register existing HashFunc with different Guava HashFunction. Got: " + hashFunction + ", Expected: " + func.hashFunction);
            }
            func = new HashFunc(name, hashFunction);
            HASH_FUNCS.put(name, func);
            return func;
        }

        /**
         * Gets an immutable list of all registered HashFuncs.
         *
         * @return All the HashFuncs registered.
         */
        public Map<String, HashFunc> getAllFuncs() {
            return Collections.unmodifiableMap(HASH_FUNCS);
        }

        /**
         * A unique name describing this {@link HashFunc}, Usually the name of the Algorithm.
         *
         * @return The name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the Guava {@link HashFunction} for this {@link HashFunc}.
         *
         * @return The HashFunction.
         */
        public HashFunction getHashFunction() {
            return hashFunction;
        }

        /**
         * Creates a new {@link Hasher} from the underlying {@link HashFunction}.
         *
         * @return The new {@link Hasher}.
         */
        public Hasher newHasher() {
            return hashFunction.newHasher();
        }
    }

}
