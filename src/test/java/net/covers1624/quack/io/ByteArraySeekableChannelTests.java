/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Created by covers1624 on 15/3/24.
 */
public class ByteArraySeekableChannelTests extends IOTestBase {

    @Test
    public void testSimple() throws IOException {
        byte[] data = randomData(64 * 1024);
        byte[] data2 = randomData(64 * 1024);
        byte[] dataDouble = Arrays.copyOf(data, data.length * 2);
        System.arraycopy(data, 0, dataDouble, data.length, data.length);
        ByteArraySeekableChannel channel = new ByteArraySeekableChannel();
        channel.write(ByteBuffer.wrap(data2));
        channel.position(0);
        channel.write(ByteBuffer.wrap(data));
        channel.write(ByteBuffer.wrap(data));
        channel.position(0);
        assertArrayEquals(dataDouble, IOUtils.toBytes(Channels.newInputStream(channel)));
    }
}
