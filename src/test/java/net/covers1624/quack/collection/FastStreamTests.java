/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 5/10/21.
 */
public class FastStreamTests {

    @Test
    public void testEmpty() {
        assertTrue(FastStream.empty().isEmpty());
        assertTrue(FastStream.of().isEmpty());

        assertFalse(FastStream.of("a").isEmpty());
    }

    @Test
    public void testOfSingle() {
        List<String> entries = FastStream.of("Single").toList();
        assertEquals(1, entries.size());
        assertEquals("Single", entries.get(0));
    }

    @Test
    public void testOfNullable() {
        List<String> entries = FastStream.<String>ofNullable(null).toList();
        assertEquals(0, entries.size());

        entries = FastStream.ofNullable("Single").toList();
        assertEquals(1, entries.size());
        assertEquals("Single", entries.get(0));
    }

    @Test
    public void testOfOptional() {
        List<String> entries = FastStream.<String>of(Optional.empty()).toList();
        assertEquals(0, entries.size());

        entries = FastStream.of(Optional.of("Single")).toList();
        assertEquals(1, entries.size());
        assertEquals("Single", entries.get(0));
    }

    @Test
    public void testOfVarargs() {
        List<String> entries = FastStream.of("A", "B", "C").toList();
        assertEquals(3, entries.size());
        assertEquals("A", entries.get(0));
        assertEquals("B", entries.get(1));
        assertEquals("C", entries.get(2));
    }

    @Test
    public void testConcat() {
        List<String> entries = FastStream.of("A", "B", "C").concat(FastStream.of("D", "E", "F")).toList();
        assertEquals(6, entries.size());
        assertEquals("A", entries.get(0));
        assertEquals("B", entries.get(1));
        assertEquals("C", entries.get(2));
        assertEquals("D", entries.get(3));
        assertEquals("E", entries.get(4));
        assertEquals("F", entries.get(5));
    }

    @Test
    public void testToList() {
        List<String> entries = FastStream.of("a", "b", "c", "d").toList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testFilter() {
        List<String> entries = FastStream.of("a", "b", "c", "d")
                .filter(e -> !e.equals("c"))
                .toList();
        assertEquals(3, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("d", entries.get(2));
    }

    @Test
    public void testFilterNot() {
        List<String> entries = FastStream.of("a", "b", "c", "d")
                .filterNot(e -> e.equals("c"))
                .toList();
        assertEquals(3, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("d", entries.get(2));
    }

    @Test
    public void testMap() {
        List<String> entries = FastStream.of("a", "b", "c", "d")
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
        List<String> entries = FastStream.of(Arrays.asList("a", "b"), Arrays.asList("c", "d"))
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
        List<String> entries = FastStream.of("a", "a", "a", "b", "b", "b", "c", "c", "c", "d", "d", "d")
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
        List<String> entries = FastStream.of("a", "b", "c", "d")
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
        List<String> baseIterable = ImmutableList.of("a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c", "d");

        assertStreamEquals(ImmutableList.of("a", "b", "c", "d"), () -> FastStream.of(baseIterable).limit(4));
        assertStreamEquals(baseIterable, () -> FastStream.of(baseIterable).limit(-1));
        assertStreamEquals(baseIterable, () -> FastStream.of(baseIterable).limit(12));
        assertStreamEquals(ImmutableList.of(), () -> FastStream.of(baseIterable).limit(0));
    }

    @Test
    public void testSkip() {
        List<String> baseIterable = ImmutableList.of("a", "b", "c", "d", "a", "b", "c", "d", "a", "b", "c", "d");

        assertStreamEquals(ImmutableList.of("a", "b", "c", "d"), () -> FastStream.of(baseIterable).skip(8));
        assertStreamEquals(baseIterable, () -> FastStream.of(baseIterable).skip(0));
        assertStreamEquals(ImmutableList.of(), () -> FastStream.of(baseIterable).skip(12));
    }

    @Test
    public void testToArray() {
        Object[] objectArray = FastStream.of("a", "b", "c", "d").toArray();
        assertEquals(4, objectArray.length);
        assertEquals("a", objectArray[0]);
        assertEquals("b", objectArray[1]);
        assertEquals("c", objectArray[2]);
        assertEquals("d", objectArray[3]);

        String[] stringArray = FastStream.of("a", "b", "c", "d").toArray(new String[0]);
        assertEquals(4, stringArray.length);
        assertEquals("a", stringArray[0]);
        assertEquals("b", stringArray[1]);
        assertEquals("c", stringArray[2]);
        assertEquals("d", stringArray[3]);

        String[] specificArray = FastStream.of("a", "b", "c", "d").toArray(String[]::new);
        assertEquals(4, specificArray.length);
        assertEquals("a", specificArray[0]);
        assertEquals("b", specificArray[1]);
        assertEquals("c", specificArray[2]);
        assertEquals("d", specificArray[3]);
    }

    @Test
    public void testFold() {
        Optional<String> sOption = FastStream.of("a", "b", "c", "d")
                .fold((a, b) -> a + b);
        assertTrue(sOption.isPresent());
        assertEquals("abcd", sOption.get());

        assertFalse(FastStream.<String>empty().fold((a, b) -> a + b).isPresent());
    }

    @Test
    public void testFoldWithIdentity() {
        String s = FastStream.of("a", "b", "c", "d")
                .fold("identity_", (a, b) -> a + b);
        assertEquals("identity_abcd", s);

        assertEquals(FastStream.<String>empty().fold("identity", (a, b) -> a + b), "identity");
    }

    @Test
    public void testCount() {
        int count = FastStream.of("a", "b", "c", "d")
                .count();

        assertEquals(4, count);
    }

    @Test
    public void testIntSum() {
        int sum = FastStream.of("one", "two", "three")
                .intSum(String::length);

        assertEquals(11, sum);
    }

    @Test
    public void testLongSum() {
        long sum = FastStream.of("one", "two", "three")
                .longSum(String::length);

        assertEquals(11L, sum);
    }

    @Test
    public void testDoubleSum() {
        double sum = FastStream.of("one", "two", "three")
                .doubleSum(e -> e.length() / 2D);

        assertEquals(5.5D, sum);
    }

    @Test
    public void testAnyMatch() {
        assertTrue(FastStream.of("a", "b", "c", "d").anyMatch(e -> e.equals("a")));
        assertFalse(FastStream.of("a", "b", "c", "d").anyMatch(e -> e.equals("e")));
    }

    @Test
    public void testAllMatch() {
        assertTrue(FastStream.of("a", "b", "c", "d").allMatch(e -> e.length() == 1));
        assertFalse(FastStream.of("a", "b", "c", "d", "ef").allMatch(e -> e.length() == 1));
    }

    @Test
    public void testNoneMatch() {
        assertTrue(FastStream.of("a", "b", "c", "d").noneMatch(e -> e.length() == 2));
        assertFalse(FastStream.of("a", "b", "c", "d", "ef").noneMatch(e -> e.length() == 2));
    }

    @Test
    public void testFindFirst() {
        Optional<String> optional = FastStream.of("a", "b", "c", "d").findFirst();
        assertTrue(optional.isPresent());
        assertEquals("a", optional.get());

        optional = FastStream.<String>empty().findFirst();
        assertFalse(optional.isPresent());
    }

    @Test
    public void testFirst() {
        assertEquals("a", FastStream.of("a").first());
        assertEquals("a", FastStream.of("a", "b").first());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> FastStream.empty().first());
        assertEquals("Not found.", ex.getMessage());
    }

    @Test
    public void testFirstOrDefault() {
        assertEquals("a", FastStream.of("a").firstOrDefault());
        assertEquals("a", FastStream.of("a", "b").firstOrDefault());
        assertNull(FastStream.empty().firstOrDefault());
    }

    @Test
    public void testFindLast() {
        Optional<String> optional = FastStream.of("a", "b", "c", "d").findLast();
        assertTrue(optional.isPresent());
        assertEquals("d", optional.get());

        optional = FastStream.<String>empty().findLast();
        assertFalse(optional.isPresent());
    }

    @Test
    public void testLast() {
        assertEquals("a", FastStream.of("a").last());
        assertEquals("b", FastStream.of("a", "b").last());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> FastStream.empty().last());
        assertEquals("Not found.", ex.getMessage());
    }

    @Test
    public void testLastOrDefault() {
        assertEquals("a", FastStream.of("a").lastOrDefault());
        assertEquals("b", FastStream.of("a", "b").lastOrDefault());
        assertNull(FastStream.empty().lastOrDefault());
    }

    @Test
    public void testOnly() {
        assertEquals("a", FastStream.of("a").only());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> FastStream.of("a", "b").only());
        assertEquals("More than one element.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, () -> FastStream.empty().only());
        assertEquals("Not found.", ex.getMessage());
    }

    @Test
    public void testOnlyOrDefault() {
        assertEquals("a", FastStream.of("a").onlyOrDefault());
        assertNull(FastStream.of("a", "b").onlyOrDefault());
        assertNull(FastStream.empty().onlyOrDefault());
    }

    @Test
    public void testToLinkedList() {
        List<String> entries = FastStream.of("a", "b", "c", "d").toLinkedList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    @Test
    public void testToSet() {
        HashSet<String> entries = FastStream.of("a", "b", "c", "d").toSet();
        assertEquals(4, entries.size());
        assertTrue(entries.contains("a"));
        assertTrue(entries.contains("b"));
        assertTrue(entries.contains("c"));
        assertTrue(entries.contains("d"));
    }

    @Test
    public void testToMap() {
        HashMap<String, String> entries = FastStream.of("a", "b", "c", "d")
                .toMap(e -> e, e -> "value_" + e);
        assertEquals(4, entries.size());
        assertEquals("value_a", entries.get("a"));
        assertEquals("value_b", entries.get("b"));
        assertEquals("value_c", entries.get("c"));
        assertEquals("value_d", entries.get("d"));
    }

    @Test
    public void testToMapWithMerge() {
        HashMap<String, String> entries = FastStream.of("a", "b", "c", "d", "c", "d")
                .toMap(e -> e, e -> "value_" + e, (a, b) -> a + "_" + b);
        assertEquals(4, entries.size());
        assertEquals("value_a", entries.get("a"));
        assertEquals("value_b", entries.get("b"));
        assertEquals("value_c_value_c", entries.get("c"));
        assertEquals("value_d_value_d", entries.get("d"));
    }

    @Test
    public void testGroupBy() {
        Map<Character, List<String>> grouped = FastStream.of("apple", "banana", "boat", "pair", "pool")
                .groupBy(e -> e.charAt(0))
                .toMap(FastStream.Group::getKey, FastStream::toList);
        assertEquals(3, grouped.size());
        assertCollectionEquals(grouped.get('a'), "apple");
        assertCollectionEquals(grouped.get('b'), "banana", "boat");
        assertCollectionEquals(grouped.get('p'), "pair", "pool");
    }

    @Test
    public void testPartition() {
        List<String> b0 = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        List<String> b1 = Arrays.asList("k", "l", "m", "n", "o", "p", "q", "r", "s", "t");
        List<String> b2 = Arrays.asList("u", "v", "w", "x", "y", "z");
        List<List<String>> partitioned = FastStream.of(b0, b1, b2).flatMap(e -> e)
                .partition(10)
                .map(FastStream::toList)
                .toList(FastStream.infer());
        assertEquals(3, partitioned.size());
        assertEquals(b0, partitioned.get(0));
        assertEquals(b1, partitioned.get(1));
        assertEquals(b2, partitioned.get(2));
    }

    @Test
    public void testIndexed() {
        List<FastStream.IndexedEntry<String>> indexed = FastStream.of("apple", "banana", "boat")
                .indexed()
                .toList();
        assertEquals(3, indexed.size());
        FastStream.IndexedEntry<String> apple = indexed.get(0);
        assertEquals(0, apple.key);
        assertEquals("apple", apple.value);
        FastStream.IndexedEntry<String> banana = indexed.get(1);
        assertEquals(1, banana.key);
        assertEquals("banana", banana.value);
        FastStream.IndexedEntry<String> boat = indexed.get(2);
        assertEquals(2, boat.key);
        assertEquals("boat", boat.value);
    }

    @Test
    public void testJoin() {
        assertEquals("apple, banana, boat, pair, pool", FastStream.of("apple", "banana", "boat", "pair", "pool").join(", "));
    }

    private void assertCollectionEquals(@Nullable Collection<?> col, Object... values) {
        assertNotNull(col);
        assertEquals(values.length, col.size());
        int i = 0;
        for (Object o : col) {
            assertEquals(values[i++], o);
        }
    }

    @Test
    public void testSorted() {
        List<String> entries = FastStream.of("b", "a", "c", "d")
                .sorted()
                .toList();

        assertEquals(entries, ImmutableList.of("a", "b", "c", "d"));

        entries = new ArrayList<>();
        FastStream.of("b", "a", "c", "d")
                .sorted()
                .forEach(entries::add);

        assertEquals(entries, ImmutableList.of("a", "b", "c", "d"));
    }

    @Test
    public void testReversed() {
        List<String> entries = ImmutableList.of("a", "b", "c", "d");
        assertStreamEquals(Lists.reverse(entries), () -> FastStream.of(entries).reversed());

        List<String> entries2 = ImmutableList.of("a", "b", "c", "d", "e");
        assertStreamEquals(Lists.reverse(entries2), () -> FastStream.of(entries2).reversed());
    }

    @Test
    public void regressionForEachAbortCollision() {
        assertTrue(FastStream.of("a", "b").skip(1).allMatch(e -> e.equals("b")));
        assertFalse(FastStream.of("a", "b").skip(1).allMatch(e -> e.equals("a")));
    }

    @Test
    public void testSpliteratorInput() {
        List<String> list = ImmutableList.of("a", "b", "c", "d");
        List<String> entries = FastStream.of(list.spliterator()).toLinkedList();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0));
        assertEquals("b", entries.get(1));
        assertEquals("c", entries.get(2));
        assertEquals("d", entries.get(3));
    }

    private <T> void assertStreamEquals(List<T> expected, Supplier<FastStream<T>> stream) {
        assertEquals(expected.size(), stream.get().count());
        assertEquals(expected, stream.get().toList());
        assertIterableEquals(expected, stream.get());

        FastStream<T> s = stream.get();
        assertEquals(expected.size(), s.count());
        assertEquals(expected, s.toList());

        s = stream.get();
        assertEquals(expected, s.toList());
        assertIterableEquals(expected, s);

        s = stream.get();
        assertIterableEquals(expected, s);
        assertEquals(expected, s.toList());

        s = stream.get();
        assertEquals(expected.size(), s.count());
        assertIterableEquals(expected, s);
    }

    private <T> void assertIterableEquals(Iterable<T> expected, Iterable<T> actual) {
        Iterator<T> it = actual.iterator();
        for (T s : expected) {
            assertTrue(it.hasNext());
            assertEquals(it.next(), s);
        }
        assertFalse(it.hasNext());
    }
}
