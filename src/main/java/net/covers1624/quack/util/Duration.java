/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import java.util.concurrent.TimeUnit;

/**
 * Represents a {@link TimeUnit} and duration in that unit.
 * <p>
 * Created by covers1624 on 16/1/24.
 */
public class Duration {

    public final TimeUnit unit;
    public final long time;

    public Duration(TimeUnit unit, long time) {
        this.unit = unit;
        this.time = time;
    }

    // @formatter:off
    public static Duration nanos(long time) { return new Duration(TimeUnit.NANOSECONDS, time); }
    public static Duration micros(long time) { return new Duration(TimeUnit.MICROSECONDS, time); }
    public static Duration millis(long time) { return new Duration(TimeUnit.MILLISECONDS, time); }
    public static Duration seconds(long time) { return new Duration(TimeUnit.SECONDS, time); }
    public static Duration minutes(long time) { return new Duration(TimeUnit.MINUTES, time); }
    public static Duration days(long time) { return new Duration(TimeUnit.DAYS, time); }
    // @formatter:on
}
