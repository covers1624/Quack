/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.platform;

/**
 * Represents an OperatingSystem architecture.
 * <p>
 * Created by covers1624 on 10/30/21.
 */
public enum Architecture {
    X86,
    X64,
    /**
     * ARMv7.
     */
    ARM,
    /**
     * AArch64. Mac M1, ARMv8+.
     */
    AARCH64,
    /**
     * PowerPC.
     */
    PPC,
    /**
     * Represents an Unknown Architecture.
     */
    UNKNOWN;

    private static final Architecture CURRENT = parse(System.getProperty("os.arch"));

    /**
     * Returns the current system's Architecture.
     *
     * @return The {@link Architecture}.
     */
    public static Architecture current() {
        return CURRENT;
    }

    /**
     * Parse an architecture string.
     * <p>
     * This method will support the result of {@link System#getProperty} with
     * the key of <code>os.arch</code>.
     *
     * @param arch The Arch to parse.
     * @return The {@link Architecture}. Will return {@link #UNKNOWN} if the
     * architecture is not known.
     */
    public static Architecture parse(String arch) {
        switch (arch.toLowerCase()) {
            case "i386":
            case "x86":
                return X86;
            case "x64":
            case "x86_64":
            case "amd64":
                return X64;
            case "arm":
                return ARM;
            case "ppc":
                return PPC;
            case "aarch64":
                return AARCH64;
            default:
                return UNKNOWN;
        }
    }
}
