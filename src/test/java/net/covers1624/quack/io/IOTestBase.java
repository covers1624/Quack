/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.util.*;

/**
 * Created by covers1624 on 12/3/21.
 */
public class IOTestBase {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static Map<String, List<String>> generateRandomFiles(Random randy) {
        Map<String, List<String>> files = new HashMap<>();

        int numFiles = 10 + randy.nextInt(5);
        for (int i = 0; i < numFiles; i++) {
            String fileName = generateRandomHex(randy, 8) + ".txt";
            files.put(fileName, generateRandomFile(randy));
        }

        return files;
    }

    public static List<String> generateRandomFile(Random randy) {
        int numLines = 10 + randy.nextInt(100);
        List<String> lines = new ArrayList<>(numLines);
        for (int j = 0; j < numLines; j++) {
            lines.add(generateRandomHex(randy, 5 + randy.nextInt(100)));
        }
        return lines;
    }

    public static String generateRandomHex(Random randy, int len) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(HEX[randy.nextInt(HEX.length)]);
        }
        return builder.toString();
    }

    public static byte[] randomData(int len) {
        Random randy = new Random();
        byte[] data = new byte[len];
        randy.nextBytes(data);
        return data;
    }
}
