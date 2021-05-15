/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
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
