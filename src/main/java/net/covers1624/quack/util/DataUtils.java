/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

/**
 * Created by covers1624 on 22/11/21.
 */
public class DataUtils {

    /**
     * Formats the provided number of bytes into a Human readable string.
     * <p>
     * Anything above 1024 Peta Bytes will still be displayed as Peta Bytes.
     *
     * @param bytes The bytes.
     * @return The human string.
     */
    public static String humanSize(long bytes) {
        if (bytes < 1024L) return bytes + " B";
        if (bytes < 1024L * 1024L) return (bytes / 1024) + " KB";
        if (bytes < 1024L * 1024L * 1024L) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        if (bytes < 1024L * 1024L * 1024L * 1024L) return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        if (bytes < 1024L * 1024L * 1024L * 1024L * 1024) return String.format("%.2f TB", bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0));

        return String.format("%.2f PB", bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Formats the provided number of bytes into a Human readable speed.
     * <p>
     * Anything above 1024 Peta Bytes will still be displayed as Peta Bytes.
     *
     * @param bytes The bytes.
     * @return The human string.
     */
    public static String humanSpeed(double bytes) {
        if (bytes < 1024D) return String.format("%.2f B/s", bytes);
        if (bytes < 1024D * 1024D) return String.format("%.2f KB/s", (bytes / 1024));
        if (bytes < 1024D * 1024D * 1024D) return String.format("%.2f MB/s", bytes / (1024.0 * 1024.0));
        if (bytes < 1024D * 1024D * 1024D * 1024D) return String.format("%.2f GB/s", bytes / (1024.0 * 1024.0 * 1024D));
        if (bytes < 1024D * 1024D * 1024D * 1024D * 1024D) return String.format("%.2f TB/s", bytes / (1024.0 * 1024.0 * 1024D * 1024D));

        return String.format("%.2f PB/s", bytes / (1024.0 * 1024.0 * 1024.0 * 1024D * 1024D));
    }
}
