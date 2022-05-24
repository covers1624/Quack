/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.hashing;

import net.covers1624.quack.util.HashUtils;
import net.covers1624.quack.util.HashUtilsTests;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 24/5/22.
 */
@SuppressWarnings ("UnstableApiUsage")
public class Murmur2HashFunctionTests {

    @Test
    public void test() throws Throwable {
        Path temp = Files.createTempDirectory("hashing");
        temp.toFile().deleteOnExit();
        Path testFile = temp.resolve("hash.txt");
        copyTestFile(testFile);

        assertEquals(0xFB0D62A5, HashUtils.hash(new Murmur2HashFunction(), testFile).asInt());
    }

    @Test
    public void testNormalized() throws Throwable {
        Path temp = Files.createTempDirectory("hashing");
        temp.toFile().deleteOnExit();
        Path testFile = temp.resolve("hash.txt");
        copyTestFile(testFile);

        assertEquals(0x5FD3DC1B, HashUtils.hash(new Murmur2HashFunction(true), testFile).asInt());
    }

    private static void copyTestFile(Path dst) throws Throwable {
        Path resource = Paths.get(HashUtilsTests.class.getResource("/to_hash.txt").toURI());
        Files.copy(resource, dst, StandardCopyOption.REPLACE_EXISTING);
    }
}
