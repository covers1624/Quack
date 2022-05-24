/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.hashing;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import net.covers1624.quack.annotation.Requires;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by covers1624 on 18/3/22.
 */
@Requires ("com.google.guava:guava")
@SuppressWarnings ("UnstableApiUsage")
public class Murmur2HashFunction implements HashFunction {

    private final int seed;
    private final boolean normalizeWhitespace;

    public Murmur2HashFunction() {
        this(1, false);
    }

    public Murmur2HashFunction(int seed) {
        this(seed, false);
    }

    public Murmur2HashFunction(boolean normalizeWhitespace) {
        this(1, normalizeWhitespace);
    }

    public Murmur2HashFunction(int seed, boolean normalizeWhitespace) {
        this.seed = seed;
        this.normalizeWhitespace = normalizeWhitespace;
    }

    // @formatter:off
    @Override public Hasher newHasher() { return new Murmur2Hasher(); }
    @Override public Hasher newHasher(int expectedInputSize) { return new Murmur2Hasher(expectedInputSize); }
    @Override public HashCode hashInt(int input) { return newHasher(4).putInt(input).hash(); }
    @Override public HashCode hashLong(long input) { return newHasher(8).putLong(input).hash(); }
    @Override public HashCode hashBytes(byte[] input) { return newHasher(input.length).putBytes(input).hash(); }
    @Override public HashCode hashBytes(byte[] input, int off, int len) { return newHasher(len).putBytes(input, off, len).hash(); }
    @Override public HashCode hashBytes(ByteBuffer input) { return newHasher(input.remaining()).putBytes(input).hash(); }
    @Override public HashCode hashUnencodedChars(CharSequence input) { return newHasher(input.length() * 2).putUnencodedChars(input).hash(); }
    @Override public HashCode hashString(CharSequence input, Charset charset) { return hashBytes(input.toString().getBytes(charset)); }
    // @formatter:on

    @Override
    public <T> HashCode hashObject(T instance, Funnel<? super T> funnel) {
        Hasher hasher = newHasher();
        funnel.funnel(instance, hasher);
        return hasher.hash();
    }

    @Override
    public int bits() {
        return 32;
    }

    private class Murmur2Hasher implements Hasher {

        private final ByteArrayOutputStream bos;

        public Murmur2Hasher() {
            bos = new ByteArrayOutputStream();
        }

        public Murmur2Hasher(int expectedInputSize) {
            bos = new ByteArrayOutputStream(expectedInputSize);
        }

        @Override
        public Hasher putByte(byte b) {
            if (normalizeWhitespace && (b == '\t' || b == '\n' || b == '\r' || b == ' ')) {
                return this;
            }
            bos.write(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes) {
            return putBytes(bytes, 0, bytes.length);
        }

        @Override
        public Hasher putBytes(byte[] bytes, int off, int len) {
            if ((off < 0) || (off > bytes.length) || (len < 0) || ((off + len) - bytes.length > 0)) {
                throw new IndexOutOfBoundsException("b.len: " + bytes.length + " off: " + off + " len: " + len);
            }
            for (int i = off; i < len; i++) {
                putByte(bytes[i]);
            }
            return this;
        }

        @Override
        public Hasher putBytes(ByteBuffer b) {
            if (b.hasArray()) {
                putBytes(b.array(), b.arrayOffset() + b.position(), b.remaining());
                b.position(b.limit());
            } else {
                for (int i = b.remaining(); i > 0; i--) {
                    putByte(b.get());
                }
            }
            return this;
        }

        @Override
        public Hasher putShort(short s) {
            putByte((byte) s);
            putByte((byte) (s >>> 8));
            return this;
        }

        @Override
        public Hasher putInt(int i) {
            putByte((byte) i);
            putByte((byte) (i >>> 8));
            putByte((byte) (i >>> 16));
            putByte((byte) (i >>> 24));
            return this;
        }

        @Override
        public Hasher putLong(long l) {
            for (int i = 0; i < 64; i += 8) {
                putByte((byte) (l >>> i));
            }
            return this;
        }

        @Override
        public Hasher putFloat(float f) {
            return putInt(Float.floatToRawIntBits(f));
        }

        @Override
        public Hasher putDouble(double d) {
            return putLong(Double.doubleToRawLongBits(d));
        }

        @Override
        public Hasher putBoolean(boolean b) {
            return putByte(b ? (byte) 1 : (byte) 0);
        }

        @Override
        public Hasher putChar(char c) {
            putByte((byte) c);
            putByte((byte) (c >>> 8));
            return this;
        }

        @Override
        public Hasher putUnencodedChars(CharSequence charSequence) {
            for (int i = 0, len = charSequence.length(); i < len; i++) {
                putChar(charSequence.charAt(i));
            }
            return this;
        }

        @Override
        public Hasher putString(CharSequence charSequence, Charset charset) {
            return putBytes(charSequence.toString().getBytes(charset));
        }

        @Override
        public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
            funnel.funnel(instance, this);
            return this;
        }

        @Override
        public HashCode hash() {
            byte[] bytes = bos.toByteArray();
            int len = bytes.length;

            int m = 0x5bd1e995;
            int r = 24;

            int h = seed ^ len;

            int index = 0;
            while (len >= 4) {
                int k = readInt(bytes, index);
                k *= m;
                k ^= k >>> r;
                k *= m;

                h *= m;
                h ^= k;
                index += 4;
                len -= 4;
            }

            switch (len) {
                case 3:
                    h ^= (bytes[index + 2] & 0xFF) << 16;
                case 2:
                    h ^= (bytes[index + 1] & 0xFF) << 8;
                case 1:
                    h ^= bytes[index] & 0xFF;
                    h *= m;
                    break;
            }

            h ^= h >>> 13;
            h *= m;
            h ^= h >>> 15;

            return HashCode.fromInt(h);
        }
    }

    private static int readInt(byte[] bytes, int index) {
        return (bytes[index + 3] & 0xFF) << 24 | (bytes[index + 2] & 0xFF) << 16 | (bytes[index + 1] & 0xFF) << 8 | bytes[index] & 0xFF;
    }
}
