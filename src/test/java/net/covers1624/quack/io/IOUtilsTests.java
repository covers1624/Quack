/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 31/12/21.
 */
public class IOUtilsTests extends IOTestBase {

    @Test
    public void testParseMode() {
        assertEquals(PosixFilePermissions.fromString("---------"), IOUtils.parseMode(0));
        assertEquals(PosixFilePermissions.fromString("r--------"), IOUtils.parseMode(400));
        assertEquals(PosixFilePermissions.fromString("-w-------"), IOUtils.parseMode(200));
        assertEquals(PosixFilePermissions.fromString("--x------"), IOUtils.parseMode(100));
        assertEquals(PosixFilePermissions.fromString("---r-----"), IOUtils.parseMode(40));
        assertEquals(PosixFilePermissions.fromString("----w----"), IOUtils.parseMode(20));
        assertEquals(PosixFilePermissions.fromString("-----x---"), IOUtils.parseMode(10));
        assertEquals(PosixFilePermissions.fromString("------r--"), IOUtils.parseMode(4));
        assertEquals(PosixFilePermissions.fromString("-------w-"), IOUtils.parseMode(2));
        assertEquals(PosixFilePermissions.fromString("--------x"), IOUtils.parseMode(1));
        assertEquals(PosixFilePermissions.fromString("rwxr-xr-x"), IOUtils.parseMode(755));
        assertEquals(PosixFilePermissions.fromString("rwxrwxrwx"), IOUtils.parseMode(777));
    }

    @Test
    public void testWriteMode() {
        assertEquals(0, IOUtils.writeMode(PosixFilePermissions.fromString("---------")));
        assertEquals(400, IOUtils.writeMode(PosixFilePermissions.fromString("r--------")));
        assertEquals(200, IOUtils.writeMode(PosixFilePermissions.fromString("-w-------")));
        assertEquals(100, IOUtils.writeMode(PosixFilePermissions.fromString("--x------")));
        assertEquals(40, IOUtils.writeMode(PosixFilePermissions.fromString("---r-----")));
        assertEquals(20, IOUtils.writeMode(PosixFilePermissions.fromString("----w----")));
        assertEquals(10, IOUtils.writeMode(PosixFilePermissions.fromString("-----x---")));
        assertEquals(4, IOUtils.writeMode(PosixFilePermissions.fromString("------r--")));
        assertEquals(2, IOUtils.writeMode(PosixFilePermissions.fromString("-------w-")));
        assertEquals(1, IOUtils.writeMode(PosixFilePermissions.fromString("--------x")));
        assertEquals(755, IOUtils.writeMode(PosixFilePermissions.fromString("rwxr-xr-x")));
        assertEquals(777, IOUtils.writeMode(PosixFilePermissions.fromString("rwxrwxrwx")));
    }

    @Test
    public void testFill() throws IOException {
        byte[] data = randomData(16);
        ByteBuffer buffer = ByteBuffer.allocate(data.length);
        // @formatter:off
        ReadableByteChannel channel = new ReadableByteChannel() {
            @Override
            public int read(ByteBuffer dst) throws IOException {
                if (!dst.hasRemaining()) return -1;
                dst.put(data[dst.position()]);
                return 1;
            }
            @Override public boolean isOpen() { return true; }
            @Override public void close() { }
        };
        // @formatter:on
        IOUtils.fill(channel, buffer);
        assertArrayEquals(data, buffer.array());
    }

    @Test
    public void testFillFail() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // @formatter:off
        ReadableByteChannel channel = new ReadableByteChannel() {
            @Override
            public int read(ByteBuffer dst) throws IOException {
                // Simulate buffer ending prematurely.
                if (dst.capacity() / 2 == dst.position()) return -1;
                dst.put((byte) 1);
                return 1;
            }
            @Override public boolean isOpen() { return true; }
            @Override public void close() { }
        };
        // @formatter:on
        assertThrows(EOFException.class, () -> IOUtils.fill(channel, buffer));
    }
}
