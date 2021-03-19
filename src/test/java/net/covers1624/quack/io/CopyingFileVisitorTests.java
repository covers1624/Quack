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

package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 12/3/21.
 */
public class CopyingFileVisitorTests extends IOTestBase {

    @Test
    public void testCopy() throws Throwable {
        Path src = Files.createTempDirectory("copy_src");
        Path dest = Files.createTempDirectory("copy_dest");
        src.toFile().deleteOnExit();
        dest.toFile().deleteOnExit();
        Map<String, List<String>> files = generateRandomFiles(new Random());

        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            Path filePath = src.resolve(entry.getKey());
            Files.write(filePath, entry.getValue());
        }

        Files.walkFileTree(src, new CopyingFileVisitor(src, dest));

        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            Path filePath = dest.resolve(entry.getKey());
            List<String> lines = Files.readAllLines(filePath);
            assertEquals(entry.getValue(), lines);
        }
    }

    @Test
    public void testCopyFilter() throws Throwable {
        Random randy = new Random();

        Path src = Files.createTempDirectory("copy_src");
        Path dest = Files.createTempDirectory("copy_dest");
        src.toFile().deleteOnExit();
        dest.toFile().deleteOnExit();
        Map<String, List<String>> files = new HashMap<>();

        files.put("a/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("a/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/a/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/a/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/b/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("b/b/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("c/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));
        files.put("c/" + generateRandomHex(randy, 8) + ".txt", generateRandomFile(randy));

        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            Path filePath = src.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, entry.getValue());
        }

        Files.walkFileTree(src, new CopyingFileVisitor(src, dest, e -> !e.toString().startsWith("b/")));

        assertTrue(Files.exists(dest.resolve("a")));
        assertFalse(Files.exists(dest.resolve("b")));
        assertTrue(Files.exists(dest.resolve("c")));

        for (Map.Entry<String, List<String>> entry : files.entrySet()) {
            Path filePath = dest.resolve(entry.getKey());
            if (entry.getKey().startsWith("b/")) {
                assertFalse(Files.exists(filePath));
            } else {
                List<String> lines = Files.readAllLines(filePath);
                assertEquals(entry.getValue(), lines);
            }
        }
    }
}
