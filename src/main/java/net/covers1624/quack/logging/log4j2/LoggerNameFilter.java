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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

/**
 * This filter returns the onMatch result if the logger name in the LogEvent is the same.
 * <p>
 * Created by covers1624 on 18/1/21.
 */
@Requires ("org.apache.logging.log4j:log4j-core")
@Plugin (name = "LoggerNameFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public final class LoggerNameFilter extends AbstractFilter {

    public static final String ATTR_LOGGER = "logger";
    public static final String ATTR_CONTAINS = "contains";
    private final String name;
    private final boolean contains;

    private LoggerNameFilter(String name, boolean contains, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.name = name;
        this.contains = contains;
    }

    private Result filter(Logger logger) {
        return filter(logger.getName());
    }

    private Result filter(String logger) {
        boolean match = logger != null && (contains ? logger.contains(name) : logger.equals(name));
        return match ? onMatch : onMismatch;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Creates the LoggerNameFilter.
     *
     * @param logger   The Logger name to match.
     * @param match    The action to take if a match occurs.
     * @param mismatch The action to take if no match occurs.
     * @return A LoggerNameFilter.
     */
    @PluginFactory
    public static LoggerNameFilter createFilter(
            @PluginAttribute (ATTR_LOGGER) String logger,
            @PluginAttribute (ATTR_CONTAINS) boolean contains,
            @PluginAttribute (AbstractFilterBuilder.ATTR_ON_MATCH) Result match,
            @PluginAttribute (AbstractFilterBuilder.ATTR_ON_MISMATCH) Result mismatch) {

        if (logger == null) {
            LOGGER.error("A logger must be provided for LoggerNameFilter");
            return null;
        }
        return new LoggerNameFilter(logger, contains, match, mismatch);
    }

    //@formatter:off
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) { return filter(logger); }
    @Override public Result filter(LogEvent event) { return filter(event.getLoggerName()); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) { return filter(logger); }
    @Override public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) { return filter(logger); }
    //@formatter:on

}
