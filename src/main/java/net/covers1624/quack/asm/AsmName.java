/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a member as using a JVM Internal name E.g: 'java/lang/Object'
 * Simply a marker for sanity sake.
 * <p>
 * Created by covers1624 on 2/17/20.
 */
@Retention (RetentionPolicy.SOURCE)
@Target ({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface AsmName { }
