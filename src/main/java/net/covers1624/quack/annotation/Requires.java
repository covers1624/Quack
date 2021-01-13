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

package net.covers1624.quack.annotation;

import java.lang.annotation.*;

/**
 * Specifies what Maven Dependencies something requires,
 * Used as Human readable markers for denoting what Things
 * in Quack require what maven dependencies.
 *
 * Created by covers1624 on 13/1/21.
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.SOURCE)
@Repeatable (Requires.RequiresList.class)
public @interface Requires {

    /**
     * The Maven group and module separated by a colon. E.g. 'com.google.guava:guava'
     *
     * @return The coords.
     */
    String value();

    /**
     * Denotes the minimum version bound. In some cases this might be inaccurate.
     *
     * @return The minimum version.
     */
    String minVersion() default "";

    /**
     * Denotes the maximum version bound. In some cases this might be inaccurate.
     *
     * @return The maximum version.
     */
    String maxVersion() default "";

    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.SOURCE)
    public @interface RequiresList {

        Requires[] value();
    }
}
