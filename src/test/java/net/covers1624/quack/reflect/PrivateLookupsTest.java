/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.reflect;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by covers1624 on 7/6/22.
 */
public class PrivateLookupsTest {

    @Test
    public void testPrivateLookup() throws Throwable {
        MethodHandles.Lookup lookup = PrivateLookups.getTrustedLookup();
        MethodHandle handle = lookup.findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, byte[].class, int.class, int.class));

        ClassFormatError ex = assertThrows(ClassFormatError.class, () -> {
            Class<?> clazz = (Class<?>) handle.invokeExact(PrivateLookupsTest.class.getClassLoader(), new byte[0], 0, 0);
        });
        assertEquals("Truncated class file", ex.getMessage());
    }
}
