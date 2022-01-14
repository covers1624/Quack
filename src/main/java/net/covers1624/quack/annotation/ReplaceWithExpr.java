/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Intended for use with {@link Deprecated} annotations, to denote
 * the intended replacement of a method, field, or class with the given expression pattern.
 * <p>
 * The pattern is in no known format, and intended for a developer to read and understand manually.
 * <p>
 * Created by covers1624 on 5/9/21.
 */
@Retention (RetentionPolicy.CLASS)
public @interface ReplaceWithExpr {

    String value();
}
