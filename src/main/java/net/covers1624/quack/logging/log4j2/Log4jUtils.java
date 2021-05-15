/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
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
