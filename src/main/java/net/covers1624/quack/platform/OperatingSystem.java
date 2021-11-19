/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.platform;

import java.util.Locale;

/**
 * Represents an OperatingSystem.
 * <p>
 * Contains functions for Operating System lookup, and platform specific actions.
 * <p>
 * Created by covers1624 on 11/11/21.
 */
public enum OperatingSystem {
    WINDOWS,
    MACOS,
    LINUX,
    SOLARIS,
    FREEBSD,
    /**
     * Unknown OperatingSystem. Doing operations with this OS, will throw {@link UnsupportedOperationException}'s.
     */
    UNKNOWN;

    private static final OperatingSystem CURRENT = parse(System.getProperty("os.name"));

    /**
     * Returns the current {@link OperatingSystem}.
     *
     * @return The {@link OperatingSystem}.
     */
    public static OperatingSystem current() {
        return CURRENT;
    }

    /**
     * Parse an Operating System string.
     * <p>
     * This method will support the result of {@link System#getProperty} with
     * the key of <code>os.name</code>.
     *
     * @param name The name to parse.
     * @return The {@link OperatingSystem}. Will return {@link #UNKNOWN} if the
     * Operating System is not known.
     */
    public static OperatingSystem parse(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if (name.contains("windows")) {
            return WINDOWS;
        } else if (name.contains("linux")) {
            return LINUX;
        } else if (name.contains("osx") || name.contains("os x") || name.contains("darwin")) {
            return MACOS;
        } else if (name.contains("sunos") || name.contains("solaris")) {
            return SOLARIS;
        } else if (name.contains("freebsd")) {
            return FREEBSD;
        }
        return UNKNOWN;
    }

    /**
     * @return If this Operating System is literal {@link #WINDOWS}
     */
    public boolean isWindows() {
        return this == WINDOWS;
    }

    /**
     * @return If this Operating System is literal {@link #MACOS}
     */
    public boolean isMacos() {
        return this == MACOS;
    }

    /**
     * @return If this Operating System is literal {@link #LINUX}
     */
    public boolean isLinux() {
        return this == LINUX;
    }

    /**
     * @return If this Operating System is literal {@link #SOLARIS}
     */
    public boolean isSolaris() {
        return this == SOLARIS;
    }

    /**
     * @return If this Operating System is literal {@link #FREEBSD}
     */
    public boolean isFreebsd() {
        return this == FREEBSD;
    }

    /**
     * If this Operating System is one of {@link #MACOS}, {@link #LINUX}, {@link #SOLARIS}, or {@link #FREEBSD}.
     *
     * @return If this Operating System us unix/posix-like.
     */
    public boolean isUnixLike() {
        return this == MACOS || this == LINUX || this == SOLARIS || this == FREEBSD;
    }

    /**
     * Appends the system specific common executable suffix to a file name.
     *
     * @param file The file name to append to.
     * @return The file name with the suffix attached.
     */
    public String exeSuffix(String file) {
        if (this == WINDOWS) {
            return file + ".exe";
        }
        return file;
    }
}
