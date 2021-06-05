/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.maven;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Simple Immutable class for a Maven artifacts notation.
 * <p>
 * Created by covers1624 on 19/02/19.
 */
//TODO Snapshots
public class MavenNotation implements Serializable {

    public final String group;
    public final String module;
    @Nullable
    public final String version;
    @Nullable
    public final String classifier;
    public final String extension;

    public MavenNotation(String group, String module, @Nullable String version, @Nullable String classifier, String extension) {
        this.group = Objects.requireNonNull(group, "group");
        this.module = Objects.requireNonNull(module, "module");
        this.version = isEmpty(version) ? null : version;
        this.classifier = isEmpty(classifier) ? null : classifier;
        this.extension = Objects.requireNonNull(extension, "extension");
    }

    public MavenNotation(MavenNotation other) {
        this(other.group, other.module, other.version, other.classifier, other.extension);
    }

    /**
     * Parses a Maven string to a MavenNotation instance.
     * Format: group:module[:version][:classifier][@extension]
     *
     * @param str The string.
     * @return The new MavenNotation.
     */
    public static MavenNotation parse(String str) {
        String[] segs = str.split(":");
        if (segs.length > 4 || segs.length < 2) {
            throw new RuntimeException("Invalid maven string: " + str);
        }
        String ext = "jar";
        if (segs[segs.length - 1].contains("@")) {
            String s = segs[segs.length - 1];
            int at = s.indexOf("@");
            ext = s.substring(at + 1);
            segs[segs.length - 1] = s.substring(0, at);
        }
        return new MavenNotation(segs[0], segs[1], segs.length > 2 ? segs[2] : null, segs.length > 3 ? segs[3] : null, ext);

    }

    public MavenNotation withGroup(String group) {
        return new MavenNotation(group, module, version, classifier, extension);
    }

    public MavenNotation withModule(String module) {
        return new MavenNotation(group, module, version, classifier, extension);
    }

    public MavenNotation withVersion(@Nullable String version) {
        return new MavenNotation(group, module, version, classifier, extension);
    }

    public MavenNotation withClassifier(@Nullable String classifier) {
        return new MavenNotation(group, module, version, classifier, extension);
    }

    public MavenNotation withExtension(String extension) {
        return new MavenNotation(group, module, version, classifier, extension);
    }

    /**
     * Converts this MavenNotation to a path segment, either for a URL or File path.
     * <p>
     * Format: group(dot to slash)/module/version/module-version[-classifier].extension
     *
     * @return The path segment.
     */
    public String toPath() {
        Objects.requireNonNull(version, "Version missing");
        return toModulePath() + version + "/" + toFileName();
    }

    /**
     * Converts this MavenNotation to a file name.
     * <p>
     * Format: module-version[-classifier].extension
     *
     * @return The file name.
     */
    public String toFileName() {
        Objects.requireNonNull(version, "Version missing");
        String classifier = !isEmpty(this.classifier) ? "-" + this.classifier : "";
        return module + "-" + version + classifier + "." + extension;
    }

    /**
     * Converts this MavenNotation to the module folder path.
     *
     * @return The path.
     */
    public String toModulePath() {
        return group.replace(".", "/") + "/" + module + "/";
    }

    /**
     * Converts this MavenNotation to a file relative to the given base directory.
     *
     * @param dir The base directory.
     * @return The new File.
     */
    public File toFile(File dir) {
        return new File(dir, toPath());
    }

    /**
     * Converts this MavenNotation to a Path relative to the given base directory.
     *
     * @param dir The base directory.
     * @return The Path.
     */
    public Path toPath(Path dir) {
        return dir.resolve(toPath());
    }

    /**
     * Converts this MavenNotation to a URL from the given URL.
     *
     * @param repo The repo.
     * @return The new URL.
     */
    public URL toURL(String repo) {
        try {
            return new URL(appendIfMissing(repo, "/") + toPath());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + group.hashCode();
        result = 31 * result + module.hashCode();
        result = 31 * result + (!isEmpty(version) ? version : "").hashCode();
        result = 31 * result + (!isEmpty(classifier) ? classifier : "").hashCode();
        result = 31 * result + extension.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (!(obj instanceof MavenNotation)) {
            return false;
        }
        MavenNotation other = (MavenNotation) obj;
        return Objects.equals(group, other.group)//
                && Objects.equals(module, other.module)//
                && Objects.equals(version, other.version)//
                && Objects.equals(classifier, other.classifier)//
                && Objects.equals(extension, other.extension);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(group);
        builder.append(":");
        builder.append(module);
        if (!isEmpty(version)) {
            builder.append(":");
            builder.append(version);
        }
        if (!isEmpty(classifier)) {
            builder.append(":");
            builder.append(classifier);
        }
        if (!Objects.equals(extension, "jar")) {
            builder.append("@");
            builder.append(extension);
        }
        return builder.toString();
    }

    private static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static String appendIfMissing(String str, String end) {
        if (!str.endsWith(end)) {
            return str + end;
        }
        return str;
    }
}
