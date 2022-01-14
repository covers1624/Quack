/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 22/1/21.
 */

@SuppressWarnings ("UnstableApiUsage")
public class HashUtilsTests {

    @Test
    public void testHashing() throws Throwable {
        Path temp = Files.createTempDirectory("hashing");
        temp.toFile().deleteOnExit();
        Path testFile = temp.resolve("hash.txt");
        copyTestFile(testFile);
        HashCode hash = HashUtils.hash(Hashing.sha256(), testFile);
        HashCode expectedHash = HashCode.fromString("883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd");
        assertEquals(expectedHash, hash);
    }

    @Test
    public void testHashEquals() throws Throwable {
        HashCode hash = HashCode.fromString("883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd");
        assertTrue(HashUtils.equals(hash, "883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd"));
        assertFalse(HashUtils.equals(hash, ""));
        assertFalse(HashUtils.equals(null, "883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd"));
        assertFalse(HashUtils.equals(hash, null));
        assertFalse(HashUtils.equals(null, null));
        assertFalse(HashUtils.equals(hash, "adsfkjhasdl;fjowqieyur8oasedl.fjkaywe98f"));
    }

    private static void copyTestFile(Path dst) throws Throwable {
        Path resource = Paths.get(HashUtilsTests.class.getResource("/to_hash.txt").toURI());
        Files.copy(resource, dst, StandardCopyOption.REPLACE_EXISTING);
    }
}
