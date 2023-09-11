/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by covers1624 on 11/9/23.
 */
@Nonnull
@Retention (RetentionPolicy.RUNTIME)
@TypeQualifierDefault ({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface NonNullApi {
}
