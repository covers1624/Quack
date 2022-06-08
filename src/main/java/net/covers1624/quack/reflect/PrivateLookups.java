/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.reflect;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Some nasty hax to get fully trusted {@link MethodHandles.Lookup} instances.
 * <p>
 * This has been tested on Java 8 and 17.
 * <p>
 * Created by covers1624 on 7/6/22.
 */
public class PrivateLookups {

    private static final MethodHandles.Lookup LOOKUP;

    static {
        try {
            // First we grab Unsafe.
            Field f_Unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            f_Unsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) f_Unsafe.get(null);

            // Next we grab the IMPL_LOOKUP static field using unsafe.
            Class<MethodHandles.Lookup> c_Lookup = MethodHandles.Lookup.class;
            Field f_IMPL_LOOKUP = c_Lookup.getDeclaredField("IMPL_LOOKUP");
            LOOKUP = (MethodHandles.Lookup) unsafe.getObject(c_Lookup, unsafe.staticFieldOffset(f_IMPL_LOOKUP));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a fully trusted {@link MethodHandles.Lookup}
     * capable of reflecting into any object/method/field.
     *
     * @return The fully trusted Lookup.
     */
    public static MethodHandles.Lookup getTrustedLookup() {
        return LOOKUP;
    }
}
