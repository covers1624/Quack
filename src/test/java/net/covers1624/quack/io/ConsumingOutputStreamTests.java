/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 29/9/23.
 */
@SuppressWarnings ("ArraysAsListWithZeroOrOneArgument")
public class ConsumingOutputStreamTests {

    @Test
    public void testBasics() {
        testStream(asList("Hello World"), "Hello World\n");
        testStream(asList("Hello World"), "Hello World\r\n");

        testStream(asList("Hello World", "Hello World2"), "Hello World\nHello World2\n");
        testStream(asList("Hello World", "Hello World2"), "Hello World\r\nHello World2\r\n");
    }

    @Test
    public void testEmptyLines() {
        testStream(asList(""), "\n");
        testStream(asList(""), "\r\n");

        testStream(asList("", ""), "\n\n");
        testStream(asList("", ""), "\r\n\r\n");
    }

    @Test
    public void testMixed() {
        testStream(asList("Hello World", "Hello World2"), "Hello World\r\nHello World2\n");
        testStream(asList("Hello World", "Hello World2"), "Hello World\nHello World2\r\n");
    }

    @Test
    public void testWithRemainder() {
        testStream(asList("Hello World", "Hello World2"), "Hello World\nHello World2\nThis Will be voided until a newline");
        testStream(asList("Hello World", "Hello World2"), "Hello World\r\nHello World2\r\nThis Will be voided until a newline");
    }

    @Test
    public void testLeadingWhitespace() {
        testStream(asList("  Hello World", "  Hello World2"), "  Hello World\n  Hello World2\n");
        testStream(asList("  Hello World", "  Hello World2"), "  Hello World\r\n  Hello World2\r\n");
    }

    @Test
    public void testTrailingWhitespace() {
        testStream(asList("Hello World  ", "Hello World2  "), "Hello World  \nHello World2  \n");
        testStream(asList("Hello World  ", "Hello World2  "), "Hello World  \r\nHello World2  \r\n");
    }

    @Test
    public void testMixedWhitespace() {
        testStream(asList("  Hello World", "  Hello World2  ", "Hello World3  "), "  Hello World\n  Hello World2  \nHello World3  \n");
        testStream(asList("  Hello World", "  Hello World2  ", "Hello World3  "), "  Hello World\r\n  Hello World2  \r\nHello World3  \r\n");
    }

    private static void testStream(List<String> expected, String toPrint) {
        List<String> strings = new LinkedList<>();
        PrintWriter pw = new PrintWriter(new ConsumingOutputStream(strings::add), true);
        pw.print(toPrint);
        pw.flush();

        assertEquals(expected, strings);
    }
}
