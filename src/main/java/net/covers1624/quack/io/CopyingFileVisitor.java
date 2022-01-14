/*
 * This file is part of Quack and is Licensed under the MIT License.
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
