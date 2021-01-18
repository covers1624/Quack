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

package net.covers1624.quack.logging.log4j2;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.io.NullOutputStream;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

/**
 * A simple PrintStream redirecting to a Log4j2 logger,
 * tracing the stack frame which started the log call.
 */
@Requires ("org.apache.logging.log4j:log4j-api")
public class TracingPrintStream extends PrintStream {

    private static final int BASE_DEPTH = 4;
    private final Logger logger;

    public TracingPrintStream(Logger logger) {
        super(NullOutputStream.INSTANCE);
        this.logger = logger;
    }

    private void log(String s) {
        logger.info("{}{}", getPrefix(), s);
    }

    private static String getPrefix() {
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        StackTraceElement elem = elems[Math.min(BASE_DEPTH, elems.length - 1)]; // The caller is always at BASE_DEPTH, including this call.
        if (elem.getClassName().startsWith("kotlin.io.")) {
            elem = elems[Math.min(BASE_DEPTH + 2, elems.length - 1)]; // Kotlins IoPackage masks origins 2 deeper in the stack.
        } else if (elem.getClassName().startsWith("java.lang.Throwable")) {
            elem = elems[Math.min(BASE_DEPTH + 4, elems.length - 1)];
        }
        return "[" + elem.getClassName() + ":" + elem.getMethodName() + ":" + elem.getLineNumber() + "]: ";
    }

    @Override
    public void println(Object o) {
        log(String.valueOf(o));
    }

    @Override
    public void println(String s) {
        log(s);
    }

    @Override
    public void println(boolean x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        log(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        log(String.valueOf(x));
    }
}
