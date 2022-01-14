/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection.redblack;

import net.covers1624.quack.util.SneakyUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class RedBlackTreeTests {

    @Test
    public void testConstructSeq() {
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        verify(tree);
        for (int i = 0; i < 1000; i++) {
            tree.add(i);
            verify(tree);
        }
    }

    @Test
    public void testConstructShuffled() {
        Random randy = new Random(0);
        for (int i = 0; i < 1000; i++) {
            int[] array = IntStream.range(0, i).toArray();
            for (int j = 0; j < 3; j++) {
                shuffle(array, randy);
                RedBlackTree<Integer> tree = new RedBlackTree<>();
                for (int i1 : array) {
                    tree.add(i1);
                }
                verify(tree);
            }
        }
    }

    @Test
    public void testBuildFrom() {
        for (int i = 0; i < 1000; i++) {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.buildFromValues(IntStream.range(0, i).boxed().collect(Collectors.toList()));
            verify(tree);
        }
    }

    @Test
    public void testInsert() {
        for (int i = 0; i < 1000; i++) {
            RedBlackTree<Float> tree = new RedBlackTree<>();
            tree.addAll(IntStream.range(0, 1000).mapToObj(Float::new).collect(Collectors.toList()));
            tree.insertAt(tree.find(Float.valueOf(i)), true, tree.newNode(i + 0.5F));
            verify(tree);
        }
    }

    @Test
    public void testDelete() {
        for (int i = 0; i < 1000; i++) {
            RedBlackTree<Integer> tree = new RedBlackTree<>();
            tree.addAll(IntStream.range(0, i).boxed().collect(Collectors.toList()));
            tree.remove(i);
            verify(tree);
        }
    }

    @Test
    public void testModifications() {
        Random randy = new Random(0);
        RedBlackTree<Integer> tree = new RedBlackTree<>();
        tree.addAll(IntStream.range(0, 1000).map(e -> e * 100).boxed().collect(Collectors.toList()));
        List<Integer> list = new ArrayList<>(tree);

        for (int i = 0; i < 100; i++) {
            boolean insert = tree.size() < 100 || tree.size() < 2000 && randy.nextInt(2) == 0;
            int n = 1 + randy.nextInt((insert ? tree.size() : tree.size() / 2) - 1);
            for (int j = 0; j < n; j++) {
                if (insert) {
                    int r = randy.nextInt(tree.size() + 1);
                    int a = r == 0 ? 0 : list.get(r - 1) + 1;
                    int b = r == list.size() ? Integer.MAX_VALUE : list.get(r);
                    if (a == b) continue;

                    // int v = rand.Next(a, b);
                    int v = a + randy.nextInt(b - a);
                    int s1 = list.size();
                    int s2 = tree.size();
                    list.add(r, v);
                    tree.add(v);
                    assertEquals(s1 + 1, list.size());
                    assertEquals(s2 + 1, tree.size());
                } else {
                    int r = randy.nextInt(tree.size());
                    int v = list.get(r);

                    int s1 = list.size();
                    int s2 = tree.size();
                    list.remove(r);
                    tree.remove(v);
                    assertEquals(s1 - 1, list.size());
                    assertEquals(s2 - 1, tree.size());
                }

                verify(tree);
                assertSeqEqual(list, tree);
            }
            System.out.println("Size: " + tree.size());
        }
    }

    @Test
    public void testModificationsList() {
        Random randy = new Random(0);
        RedBlackList<Integer> list = new RedBlackList<>();
        list.addAll(IntStream.range(0, 1000).map(e -> e * 100).boxed().collect(Collectors.toList()));

        for (int i = 0; i < 100; i++) {
            boolean insert = list.size() < 100 || list.size() < 2000 && randy.nextInt(2) == 0;
            int n = 1 + randy.nextInt((insert ? list.size() : list.size() / 2) - 1);
            for (int j = 0; j < n; j++) {
                if (insert) {
                    int r = randy.nextInt(list.size() + 1);
                    int a = r == 0 ? 0 : list.get(r - 1) + 1;
                    int b = r == list.size() ? Integer.MAX_VALUE : list.get(r);
                    if (a == b) continue;

                    int v = a + randy.nextInt(b - a);
                    list.add(v);
                } else {
                    list.remove(randy.nextInt(list.size()));
                }

                verify(list);
            }
            System.out.println("Size: " + list.size());
        }

    }

    @Test
    public void testListIndex() {
        RedBlackList<Integer> rbList = new RedBlackList<>();
        rbList.addAll(IntStream.range(0, 1000).boxed().collect(Collectors.toList()));
        List<Integer> list = new ArrayList<>(rbList);

        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), rbList.get(i));
        }
    }

    @Test
    public void testList() {
        Random randy = new Random(0);
        UnorderedRedBlackList<Integer> list = new UnorderedRedBlackList<>();
        list.addAll(IntStream.range(0, 1000).map(e -> 0).boxed().collect(Collectors.toList()));
        verify(list);

        for (int i = 0; i < 50; i++) {
            boolean insert = list.size() < 100 || list.size() < 2000 && randy.nextInt(2) == 0;
            int n = 1 + randy.nextInt((insert ? list.size() : list.size() / 2) - 1);
            for (int j = 0; j < n; j++) {
                if (insert) {
                    list.add(randy.nextInt(list.size() + 1), 0);
                } else {
                    list.remove(randy.nextInt(list.size()));
                }
                verify(list);
            }
            System.out.println("Size: " + list.size());
        }
    }

    @Test
    @Disabled ("Disabled, Run this test manually.")
    public void testPerformance() {
//        warmup();
        Random randyA = new Random(0);
        Random randyB = new Random(0);
        List<Object> list = new UnorderedRedBlackList<>();
        list.addAll(IntStream.range(0, 10000).mapToObj(e -> null).collect(Collectors.toList()));
        List<Object> javaList = new ArrayList<>(list);

        long s1 = System.currentTimeMillis();
        int ops = profile(randyA, list);
        long e1 = System.currentTimeMillis();

        long s2 = System.currentTimeMillis();
        profile(randyB, javaList);
        long e2 = System.currentTimeMillis();

        long t1 = (e1 - s1);
        long t2 = (e2 - s2);
        System.out.println("Op Count : " + ops);
        System.out.println("Red-Black: " + t1 + "ms");
        System.out.println("ArrayList: " + t2 + "ms");
        assertTrue(t1 * 2 < t2, "Not fast enough. " + t1 + "ms vs " + t2 + "ms");
    }

    private int profile(Random randy, List<Object> list) {
        int ops = 0;
        for (int i = 0; i < 100; i++) {
            boolean insert = list.size() < 5000 || list.size() < 20000 && randy.nextInt(2) == 0;
            int n = 1 + randy.nextInt((insert ? list.size() : list.size() / 2) - 1);
            for (int j = 0; j < n; j++) {
                if (insert) {
                    list.add(randy.nextInt(list.size() + 1), null);
                } else {
                    list.remove(randy.nextInt(list.size()));
                }
            }
            ops += n;
        }
        return ops;
    }

    private void warmup() {
        System.out.println("Warming up..");
        for (int i = 0; i < 10; i++) {
            System.currentTimeMillis();
            Random randy = new Random(0);
            List<Object> list = new UnorderedRedBlackList<>();
            list.addAll(IntStream.range(0, 1000).mapToObj(e -> null).collect(Collectors.toList()));
            List<Object> javaList = new ArrayList<>(list);

            profile(randy, list);
            profile(randy, javaList);
        }
        System.out.println();
    }

    //Based off AbstractList.equals()
    public static <T> void assertSeqEqual(Collection<T> a, Collection<T> b) {
        assertEquals(a.size(), b.size());
        Iterator<T> i1 = a.iterator();
        Iterator<T> i2 = b.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            T o1 = i1.next();
            T o2 = i2.next();
            assertEquals(o1, o2);
        }
        assertFalse(i1.hasNext());
        assertFalse(i2.hasNext());
    }

    public static <T extends RedBlackNode<T>> void verify(BaseRedBlackTree<T> tree) {
        if (tree.getRoot() != null) {
            assertNull(tree.getRoot().getParent(), "Root node has non-null parent");
        }
        verify(tree.getRoot());
    }

    public static <T extends Comparable<T>> void verify(RedBlackList<T> tree) {
        verify(SneakyUtils.<BaseRedBlackTree<?>>unsafeCast(tree));
        assertEquals(tree.size(), verifyCounts(tree.getRoot()));
        assertEquals(tree.size(), tree.toArray().length);
        for (int i = 0; i < tree.size(); i++) {
            RedBlackList<T>.Node node = tree.nodeAt(i);
            assertTrue(node.getParent() != null || tree.getRoot() == node);
        }
    }

    public static <T> void verify(UnorderedRedBlackList<T> tree) {
        verify(SneakyUtils.<BaseRedBlackTree<?>>unsafeCast(tree));
        assertEquals(tree.size(), tree.toArray().length);
        for (int i = 0; i < tree.size(); i++) {
            UnorderedRedBlackList<T>.Node node = tree.nodeAt(i);
            assertTrue(node.getParent() != null || tree.getRoot() == node);
        }
    }

    public static <T extends Comparable<T>> int verifyCounts(@Nullable RedBlackList<T>.Node tree) {
        if (tree == null) return 0;

        assertEquals(tree.count, verifyCounts(tree.getLeft()) + verifyCounts(tree.getRight()) + 1, "Count violation");
        return tree.count;
    }

    private static <T extends RedBlackNode<T>> boolean isBlack(@Nullable T node) {
        return node == null || node.isBlack();
    }

    public static <T extends RedBlackNode<T>> int verify(@Nullable RedBlackNode<T> tree) {
        if (tree == null) return 0;

        if (tree instanceof Comparable) {
            Comparable<T> comp = SneakyUtils.unsafeCast(tree);
            if (tree.getLeft() != null) {
                assertTrue(comp.compareTo(tree.getLeft()) > 0, "Order Violation");
            }
            if (tree.getRight() != null) {
                assertTrue(comp.compareTo(tree.getRight()) < 0, "Order Violation");
            }
        }

        if (tree.getLeft() != null) {
            assertEquals(tree, tree.getLeft().getParent(), "Parent Child ref Mismatch");
        }
        if (tree.getRight() != null) {
            assertEquals(tree, tree.getRight().getParent(), "Parent Child ref Mismatch");
        }
        if (tree.isRed()) {
            assertTrue(isBlack(tree.getLeft()) && isBlack(tree.getRight()), "Red Violation");
        }

        int height = verify(tree.getLeft());
        assertEquals(height, verify(tree.getRight()), "Black Violation");

        if (tree.isBlack()) {
            height++;
        }
        return height;
    }

    public static void shuffle(int[] array, Random rand) {
        for (int i = 0; i < array.length; i++) {
            int r = i + rand.nextInt(array.length - i);
            int t = array[r];
            array[r] = array[i];
            array[i] = t;
        }
    }

}
