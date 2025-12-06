/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by covers1624 on 12/6/25.
 */
public class JavaVersionTests {

    @Test
    public void testAtLeast() {
        assertTrue(JavaVersion.JAVA_21.isAtLeast(JavaVersion.JAVA_17));
        assertTrue(JavaVersion.JAVA_17.isAtLeast(JavaVersion.JAVA_17));
        assertFalse(JavaVersion.JAVA_1_8.isAtLeast(JavaVersion.JAVA_17));
    }
}
