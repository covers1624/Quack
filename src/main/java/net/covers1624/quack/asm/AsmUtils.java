/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.asm;

/**
 * Created by covers1624 on 23/6/23.
 */
public final class AsmUtils {

    /**
     * Get the internal JVM name for the given class.
     * <p>
     * E.g: {@code java/lang/Object}
     *
     * @param clazz The class to get the name for.
     * @return The classes internal name.
     */
    @AsmName
    public static String asmName(Class<?> clazz) {
        return asmName(clazz.getName());
    }

    /**
     * Get the internal JVM name for the given class name string.
     * <p>
     * E.g: {@code java.lang.Object} -> {@code java/lang/Object}
     *
     * @param name The class name to convert.
     * @return The class internal name.
     */
    @AsmName
    public static String asmName(String name) {
        return name.replace(".", "/");
    }
}
