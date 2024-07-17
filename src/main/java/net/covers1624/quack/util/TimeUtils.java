/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import org.jetbrains.annotations.Nullable;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by covers1624 on 22/11/22.
 */
public class TimeUtils {

    public static final SimpleDateFormat FORMAT_RFC1123 = parseFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    public static final SimpleDateFormat FORMAT_RFC1036 = parseFormat("EEE, dd-MMM-yy HH:mm:ss zzz");
    public static final SimpleDateFormat FORMAT_ASCTIME = parseFormat("EEE MMM d HH:mm:ss yyyy");

    protected static final SimpleDateFormat[] PATTERNS = new SimpleDateFormat[] {
            FORMAT_RFC1123,
            FORMAT_RFC1036,
            FORMAT_ASCTIME
    };

    private static final Date TWO_DIGIT_YEAR_START;

    static {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TWO_DIGIT_YEAR_START = cal.getTime();
    }

    /**
     * Attempts to parse the given date string as either an RFC 1123, RFC 1036
     * or ASC time format.
     *
     * @param str The string to parse.
     * @return The {@link Date}. Or {@code null} if the input could not be parsed.
     */
    @Nullable
    public static Date parseDate(@Nullable String str) {
        if (str == null) return null;
        if (str.length() > 1 && str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length() - 1);
        }
        for (SimpleDateFormat pattern : PATTERNS) {
            pattern.set2DigitYearStart(TWO_DIGIT_YEAR_START);
            ParsePosition parsePosition = new ParsePosition(0);
            try {
                Date date = pattern.parse(str, parsePosition);
                if (parsePosition.getIndex() != 0) {
                    return date;
                }
            } catch (Throwable ignored) {
                // Some servers return weird dates which can cause crashes with some of the formats.
                // Protect against those and just ignore them.
            }
        }
        return null;
    }

    private static SimpleDateFormat parseFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }
}
