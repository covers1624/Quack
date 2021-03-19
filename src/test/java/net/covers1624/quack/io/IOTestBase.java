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
}
