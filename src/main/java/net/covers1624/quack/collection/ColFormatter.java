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

package net.covers1624.quack.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple column formatter. Likely horrible.
 * Created by covers1624 on 6/08/18.
 */
public class ColFormatter {

    public static List<String> format(List<List<String>> input) {
        List<List<String>> cols = rotateLists(input);
        List<List<String>> newCols = new ArrayList<>();
        for (List<String> col : cols) {
            int max = ColUtils.maxBy(col, String::length).length();
            List<String> newCol = new ArrayList<>();
            for (String cell : col) {
                StringBuilder str = new StringBuilder(cell);
                if (cell.length() < max) {
                    for (int i = cell.length(); i < max; i++) {
                        str.append(" ");
                    }
                }
                newCol.add(str.toString());
            }
            newCols.add(newCol);
        }
        List<String> lines = new ArrayList<>();
        List<List<String>> rows = rotateLists(newCols);
        for (List<String> row : rows) {
            StringBuilder builder = new StringBuilder();
            for (String cell : row) {
                if (builder.capacity() != 0) {
                    builder.append(" ");
                }
                builder.append(cell);
            }
            lines.add(builder.toString());
        }
        return lines;
    }

    public static List<List<String>> rotateLists(List<List<String>> input) {
        if (input.isEmpty()) {
            return input;
        }
        List<List<String>> sqLst = toSquare(input);
        List<List<String>> cols = new ArrayList<>();
        for (int colIndex = 0; colIndex < sqLst.get(0).size(); colIndex++) {
            List<String> col = new ArrayList<>();
            for (List<String> row : sqLst) {
                col.add(row.get(colIndex));
            }
            cols.add(col);
        }
        return cols;
    }

    public static List<List<String>> toSquare(List<List<String>> input) {
        int len = ColUtils.maxBy(input, List::size).size();
        if (ColUtils.forAll(input, e -> e.size() == len)) {
            return input;
        }
        List<List<String>> out = new ArrayList<>();
        for (List<String> row : input) {
            List<String> elm = new ArrayList<>(row);
            if (row.size() != len) {
                for (int i = row.size(); i < len; i++) {
                    elm.add(" ");
                }
            }
            out.add(elm);
        }
        return out;
    }

}
