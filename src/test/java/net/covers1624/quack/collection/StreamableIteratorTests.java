/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.collection;

import org.junit.jupiter.api.Test;

import java.util.*;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 5/10/21.
 */
public class StreamableIteratorTests {

    @Test
    public void testToList() {
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d")).toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testFilter() {
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d"))
                .filter(e -> !e.equals("c"))
                .toList();
        assertEquals(3, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("d", entries.get(2));
    }

    @Test
    public void testFilterNot() {
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d"))
                .filterNot(e -> e.equals("c"))
                .toList();
        assertEquals(3, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("d", entries.get(2));
    }

    @Test
    public void testMap() {
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d"))
                .map(e -> e + "_mapped")
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a_mapped", entries.get(0));
        assertEquals("b_mapped", entries.get(1));
        assertEquals("c_mapped", entries.get(2));
        assertEquals("d_mapped", entries.get(3));
    }

    @Test
    public void testFlatMap() {
        List<String> entries = StreamableIterable.of(of(of("a", "b"), of("c", "d")))
                .flatMap(e -> e)
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testDistinct() {
        List<String> entries = StreamableIterable.of(of("a", "a", "a", "b", "b", "b", "c", "c", "c", "d", "d", "d"))
                .distinct()
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testPeek() {
        List<String> peekList = new ArrayList<>();
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d"))
                .peek(peekList::add)
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));

        assertEquals(4, peekList.size());
        assertEquals("a", peekList.get(0));
        assertEquals("b", peekList.get(1));
        assertEquals("c", peekList.get(2));
        assertEquals("d", peekList.get(3));
    }

    @Test
    public void testLimit() {
        StreamableIterable<String> baseIterable = StreamableIterable.of(of("a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c", "d"));
        List<String> entries = baseIterable
                .limit(4)
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));

        assertEquals(baseIterable, baseIterable.limit(-1));
        assertEquals(0, baseIterable.limit(0).toList().size());
    }

    @Test
    public void testSkip() {
        StreamableIterable<String> baseIterable = StreamableIterable.of(of("a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c", "d"));
        List<String> entries = baseIterable
                .skip(8)
                .toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));

        assertEquals(baseIterable, baseIterable.skip(0));
    }

    @Test
    public void testToArray() {
        Object[] objectArray = StreamableIterable.of(of("a", "b", "c", "d")).toArray();

        assertEquals(4, objectArray.length);
        assertEquals("a", objectArray[0]);
        assertEquals("b", objectArray[1]);
        assertEquals("c", objectArray[2]);
        assertEquals("d", objectArray[3]);

        Object[] stringArray = StreamableIterable.of(of("a", "b", "c", "d")).toArray(new String[0]);

        assertEquals(4, stringArray.length);
        assertEquals("a", stringArray[0]);
        assertEquals("b", stringArray[1]);
        assertEquals("c", stringArray[2]);
        assertEquals("d", stringArray[3]);
    }

    @Test
    public void testFold() {
        Optional<String> sOption = StreamableIterable.of(of("a", "b", "c", "d"))
                .fold((a, b) -> a + b);
        assertTrue(sOption.isPresent());
        assertEquals("abcd", sOption.get());

        assertFalse(StreamableIterable.<String>empty().fold((a, b) -> a + b).isPresent());
    }

    @Test
    public void testFoldWithIdentity() {
        String s = StreamableIterable.of(of("a", "b", "c", "d"))
                .fold("identity_", (a, b) -> a + b);
        assertEquals("identity_abcd", s);

        assertEquals(StreamableIterable.<String>empty().fold("identity", (a, b) -> a + b), "identity");
    }

    @Test
    public void testCount() {
        int count = StreamableIterable.of(of("a", "b", "c", "d"))
                .count();

        assertEquals(4, count);
    }

    @Test
    public void testAnyMatch() {
        assertTrue(StreamableIterable.of(of("a", "b", "c", "d")).anyMatch(e -> e.equals("a")));
        assertFalse(StreamableIterable.of(of("a", "b", "c", "d")).anyMatch(e -> e.equals("e")));
    }

    @Test
    public void testAllMatch() {
        assertTrue(StreamableIterable.of(of("a", "b", "c", "d")).allMatch(e -> e.length() == 1));
        assertFalse(StreamableIterable.of(of("a", "b", "c", "d", "ef")).allMatch(e -> e.length() == 1));
    }

    @Test
    public void testNoneMatch() {
        assertTrue(StreamableIterable.of(of("a", "b", "c", "d")).noneMatch(e -> e.length() == 2));
        assertFalse(StreamableIterable.of(of("a", "b", "c", "d", "ef")).allMatch(e -> e.length() == 2));
    }

    @Test
    public void testFindFirst() {
        Optional<String> optional = StreamableIterable.of(of("a", "b", "c", "d")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("a", optional.get());

        optional = StreamableIterable.<String>empty().findFirst();
        assertFalse(optional.isPresent());
    }

    @Test
    public void testFindLast() {
        Optional<String> optional = StreamableIterable.of(of("a", "b", "c", "d")).findLast();
        assertTrue(optional.isPresent());
        assertEquals("d", optional.get());

        optional = StreamableIterable.<String>empty().findLast();
        assertFalse(optional.isPresent());
    }

    @Test
    public void testToLinkedList() {
        List<String> entries = StreamableIterable.of(of("a", "b", "c", "d")).toLinkedList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testToSet() {
        HashSet<String> entries = StreamableIterable.of(of("a", "b", "c", "d")).toSet();
        assertEquals(4, entries.size());
        assertTrue(entries.contains("a"));
        assertTrue(entries.contains("b"));
        assertTrue(entries.contains("c"));
        assertTrue(entries.contains("d"));
    }

    @Test
    public void testToMap() {
        HashMap<String, String> entries = StreamableIterable.of(of("a", "b", "c", "d"))
                .toMap(e -> e, e -> "value_" + e);
        assertEquals(4, entries.size());
        assertEquals("value_a", entries.get("a"));
        assertEquals("value_b", entries.get("b"));
        assertEquals("value_c", entries.get("c"));
        assertEquals("value_d", entries.get("d"));
    }

    @Test
    public void testToMapWithMerge() {
        HashMap<String, String> entries = StreamableIterable.of(of("a", "b", "c", "d", "c", "d"))
                .toMap(e -> e, e -> "value_" + e, (a, b) -> a + "_" + b);
        assertEquals(4, entries.size());
        assertEquals("value_a", entries.get("a"));
        assertEquals("value_b", entries.get("b"));
        assertEquals("value_c_value_c", entries.get("c"));
        assertEquals("value_d_value_d", entries.get("d"));
    }
}
