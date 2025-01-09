/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 1/9/25.
 */
public class ColUtilsTests {

    @Test
    public void testHeadOrDefault() {
        assertEquals(1, ColUtils.headOrDefault(Arrays.asList(1)));
        assertEquals(null, ColUtils.headOrDefault(Arrays.<Object>asList()));
        assertEquals(1, ColUtils.headOrDefault(Arrays.asList(), 1));
        assertEquals(1, ColUtils.headOrDefault(Arrays.asList(1, 2), null));
        assertEquals(1, ColUtils.headOrDefault(Arrays.asList(1, 2), 3));
        assertEquals(null, ColUtils.headOrDefault(Arrays.asList(null, null), 3));
    }
}
