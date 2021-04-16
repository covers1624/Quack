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

import net.covers1624.quack.util.SneakyUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

/**
 * A {@link FileVisitor} for Path's that copies from a to b.
 * Example: <code>Files.walkFileTree(src, new CopyingFileVisitor(src, dest));</code>
 * <p>
 * Created by covers1624 on 14/6/19.
 */
public class CopyingFileVisitor extends SimpleFileVisitor<Path> {

    private final Path fromRoot;
    private final Path toRoot;
    private final Predicate<Path> predicate;

    public CopyingFileVisitor(Path fromRoot, Path toRoot) {
        this(fromRoot, toRoot, SneakyUtils.trueP());
    }

    public CopyingFileVisitor(Path fromRoot, Path toRoot, Predicate<Path> predicate) {
        this.fromRoot = fromRoot;
        this.toRoot = toRoot;
        this.predicate = predicate;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path rel = fromRoot.relativize(dir);
        if (!predicate.test(rel)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path rel = fromRoot.relativize(file);
        if (!predicate.test(rel)) {
            return FileVisitResult.CONTINUE;
        }
        Path to = toRoot.resolve(rel.toString());
        Files.createDirectories(to.getParent());
        Files.copy(file, to, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }
}
