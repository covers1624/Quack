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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;

/**
 * Created by covers1624 on 7/12/20.
 */
@Requires ("org.apache.logging.log4j:log4j-api")
@Requires ("org.apache.logging.log4j:log4j-core")
public class Log4jUtils {

    /**
     * Redirects System.out and System.err to Log4j2
     */
    public static void redirectStreams() {
        Logger stdout = LogManager.getLogger("STDOUT");
        Logger stderr = LogManager.getLogger("STDERR");
        System.setOut(new TracingPrintStream(stdout));
        System.setErr(new TracingPrintStream(stderr));
    }

    /**
     * Flushes all configured Log4j2 {@link AbstractOutputStreamAppender}s.
     *
     * @return True if the operation succeeded, false if some error happened.
     */
    public static boolean flushLogs() {
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext();
            for (org.apache.logging.log4j.core.Logger logger : ctx.getLoggers()) {
                for (Appender appender : logger.getAppenders().values()) {
                    if (appender instanceof AbstractOutputStreamAppender) {
                        ((AbstractOutputStreamAppender<?>) appender).getManager().flush();
                    }
                }
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
