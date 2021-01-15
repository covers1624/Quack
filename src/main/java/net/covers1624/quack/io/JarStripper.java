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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

import static net.covers1624.quack.io.IOUtils.getJarFileSystem;

/**
 * Created by covers1624 on 16/12/19.
 */
public class JarStripper {

    /**
     * Copes every element in a Jar file from the input to the Jar file output, where every
     * element must match the provided {@link Predicate}.
     *
     * @param input     The Input Path. This should exist.
     * @param output    The Output Path. This should not exists.
     * @param predicate The {@link Predicate} that each Entry must match.
     * @throws IOException If something went wrong during execution.
     */
    public static void stripJar(Path input, Path output, Predicate<Path> predicate) throws IOException {
        if (!Files.exists(input)) {
            throw new FileNotFoundException("Input not found. " + input);
        }
        if (Files.exists(output)) {
            throw new IOException("Output already exists. " + output);
        }
        try (FileSystem inFs = getJarFileSystem(input, true);
             FileSystem outFs = getJarFileSystem(output, true)
        ) {
            Path inRoot = inFs.getPath("/");
            Path outRoot = outFs.getPath("/");
            Files.walkFileTree(inRoot, new Visitor(inRoot, outRoot, predicate));
        }
    }

    private static class Visitor extends SimpleFileVisitor<Path> {

        private final Path inRoot;
        private final Path outRoot;
        private final Predicate<Path> predicate;

        private Visitor(Path inRoot, Path outRoot, Predicate<Path> predicate) {
            this.inRoot = inRoot;
            this.outRoot = outRoot;
            this.predicate = predicate;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path outDir = outRoot.resolve(inRoot.relativize(dir).toString());
            if (Files.notExists(outDir)) {
                Files.createDirectories(outDir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path inFile, BasicFileAttributes attrs) throws IOException {
            Path rel = inRoot.relativize(inFile);
            Path outFile = outRoot.resolve(rel.toString());
            if (predicate.test(rel)) {
                Files.copy(inFile, outFile);
            }
            return FileVisitResult.CONTINUE;
        }
    }

}
