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

package net.covers1624.quack.test;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.covers1624.quack.util.HashUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by covers1624 on 22/1/21.
 */

@SuppressWarnings ("UnstableApiUsage")
public class HashUtilsTests {

    @Test
    public void testHashing() throws Throwable {
        Path temp = Files.createTempDirectory("hashing");
        Path testFile = temp.resolve("hash.txt");
        copyTestFile(testFile);
        HashCode hash = HashUtils.hash(Hashing.sha256(), testFile);
        HashCode expectedHash = HashCode.fromString("883d139af5d6bf5b4f940d1611198c096654b45b0c1097a36d256ce209d11dfd");
        Assertions.assertEquals(expectedHash, hash);
    }

    private static void copyTestFile(Path dst) throws Throwable {
        Path resource = Paths.get(HashUtilsTests.class.getResource("/to_hash.txt").toURI());
        Files.copy(resource, dst, StandardCopyOption.REPLACE_EXISTING);
    }
}
