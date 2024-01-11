/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Created by covers1624 on 11/1/24.
 */
public class ByteArrayReadableChannelTests {

    @Test
    public void testSimple() throws IOException {
        byte[] data = randomData(64 * 1024);
        assertArrayEquals(
                data,
                IOUtils.toBytes(Channels.newInputStream(new ByteArrayReadableChannel(data)))
        );
    }

    @Test
    public void testSliced() throws IOException {
        byte[] data = randomData(64 * 1024);
        assertArrayEquals(
                Arrays.copyOfRange(data, 1024, data.length - 1024),
                IOUtils.toBytes(Channels.newInputStream(new ByteArrayReadableChannel(data, 1024, data.length - 1024)))
        );
    }

    private static byte[] randomData(int len) {
        Random randy = new Random();
        byte[] data = new byte[len];
        randy.nextBytes(data);
        return data;
    }
}
