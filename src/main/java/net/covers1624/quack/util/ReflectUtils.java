/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import static net.covers1624.quack.util.SneakyUtils.sneaky;

/**
 * Utilities for performing Reflection.
 * <p>
 * Created by covers1624 on 13/1/21.
 */
public class ReflectUtils {

    public static <T extends AccessibleObject> T makeAccessible(T thing) {
        thing.setAccessible(true);
        return thing;
    }

    @SuppressWarnings ("unchecked")
    public static <T> T getField(Field field, Object instance) {
        return (T) sneaky(() -> makeAccessible(field).get(instance));
    }

}
