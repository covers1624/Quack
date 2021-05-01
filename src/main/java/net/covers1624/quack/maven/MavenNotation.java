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
        this.version = version;
        this.classifier = classifier;
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
        return new MavenNotation(segs[0], segs[1], segs.length > 2 ? segs[2] : null, segs.length > 3 ? segs[3] : "", ext);

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
        return toModulePath() + toFileName();
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
