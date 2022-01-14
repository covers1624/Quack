/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Intended for use with {@link Deprecated} annotations, to denote
 * the intended replacement of a method, field, or class.
 * <p>
 * This annotation does not currently enforce a contract for the format of
 * this annotation's value. However, Javadoc rules should apply:
 * <pre>
 *     java.lang.String                     // Class
 *     java.lang.String#toString            // Method without param bound, or field.
 *     java.lang.String#substring(int, int) // Method with class and param bound.
 *     #substring(int, int)                 // Method with only a param bound. Assumes the current class.
 *     #substring                           // Method without a param bound. Assumes the current class.
 * </pre>
 * <p>
 * Created by covers1624 on 5/9/21.
 */
@Retention (RetentionPolicy.CLASS)
public @interface ReplaceWith {

    String value();
}
