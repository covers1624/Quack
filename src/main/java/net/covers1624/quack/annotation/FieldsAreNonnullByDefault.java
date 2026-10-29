/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jetbrains.annotations.ApiStatus;

@Nonnull
@Retention (RetentionPolicy.RUNTIME)
@TypeQualifierDefault (ElementType.FIELD)
@ApiStatus.ScheduledForRemoval(inVersion = "0.5.0")
@Deprecated
public @interface FieldsAreNonnullByDefault { }
