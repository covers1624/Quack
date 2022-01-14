/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.collection.redblack;

import net.covers1624.quack.collection.Object2IntPair;
import net.covers1624.quack.util.Object2IntFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static net.covers1624.quack.util.SneakyUtils.sneaky;
import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Based off Chicken-Bones C# RedBlack Tree library: https://github.com/Chicken-Bones/RedBlack
 * <p>
 * Created by covers1624 on 16/5/21.
 */
public class BaseRedBlackTree<N extends RedBlackNode<N>> {

    private final Collection<N> entries = makeEntriesCollection();

    @Nullable
    private N root;
    private int _version;

    protected int count;

    public Collection<N> entries() {
        return entries;
    }

    public void insertAt(@Nullable N loc, boolean right, N node) {
        count++;
        _version++;

        if (loc == null) {
            if (root != null) {
                loc = root.most(right);
            }
            if (loc == null) {
                setRoot(node);
                return;
            }
        }

        N at = loc.getChild(right);
        if (at != null) {
            right = !right;
            loc = at.most(right);
            at = loc.getChild(right);
        }

        boolean finalRight = right;
        N finalLoc = loc;
        assert sneaky(() -> {
            N prev = finalRight ? finalLoc : finalLoc.getPrev();
            N next = finalRight ? finalLoc.getNext() : finalLoc;
            orderConsistencyCheck(prev, node);
            orderConsistencyCheck(node, next);
            return true;
        });

        loc.assign(right, node);
        node.setRed(true);
        if (node.getParent() != null) {
            node.getParent().onChildrenChanged();
        }
        fixInsertion(node);
    }

    @Nullable
    public N find(Object2IntFunction<N> comp) {
        Object2IntPair<N> pair = closest(comp);
        return pair.getValue() == 0 ? pair.getKey() : null;
    }

    public Object2IntPair<N> closest(Object2IntFunction<N> comp) {
        if (root == null) {
            return new Object2IntPair<>(null, 1);
        }

        int c;
        N node = root;
        while (true) {
            c = comp.apply(node);
            if (c == 0) {
                return new Object2IntPair<>(node, c);
            }

            N next = node.getChild(c > 0);
            if (next == null) {
                return new Object2IntPair<>(node, c);
            }
            node = next;
        }
    }

    public void replace(N loc, N node) {
        assert loc.getRoot() == root;

        assert sneaky(() -> {
            orderConsistencyCheck(loc.getPrev(), node);
            orderConsistencyCheck(node, loc.getNext());
            return true;
        });

        replaceWith(loc, node);
        node.setRed(loc.isRed());
        node.setLeft(loc.getLeft());
        node.setRight(loc.getRight());

        node.onChildrenChanged();
        if (node.getParent() != null) {
            node.getParent().onChildrenChanged();
        }
    }

    public void insertRange(@Nullable N loc, Iterable<N> nodes) {
        for (N node : nodes) {
            orderConsistencyCheck(loc, node);
            insertAt(loc, true, node);
            loc = node;
        }
        assert loc != null;
        orderConsistencyCheck(loc, loc.getNext());
    }

    public void removeRange(N first, N last) {
        orderConsistencyCheck(first, last);

        N node = first;
        while (true) {
            N next = node.getNext();
            entries.remove(node);
            if (node == last) return;

            node = next;
        }
    }

    public void buildFrom(List<N> nodes) {
        if (root != null) throw new IllegalStateException("buildFrom called on non-empty tree");

        int bh = 0;
        for (int i = nodes.size() + 1; i > 1; i >>= 1) {
            bh++;
        }

        root = buildFrom(nodes, 0, nodes.size(), bh);
        count = nodes.size();
    }

    @Nullable
    public N buildFrom(List<N> nodes, int a, int b, int bh) {
        if (a == b) return null;

        int c = (a + b) / 2;
        N p = nodes.get(c);
        p.setBlack(bh > 0);
        p.setLeft(buildFrom(nodes, a, c, bh - 1));
        p.setRight(buildFrom(nodes, c + 1, b, bh - 1));
        orderConsistencyCheck(p.getLeft(), p);
        orderConsistencyCheck(p, p.getRight());
        p.onChildrenChanged();
        return p;
    }

    protected void orderConsistencyCheck(@Nullable N left, @Nullable N right) { }

    protected Entries makeEntriesCollection() {
        return new Entries();
    }

    @Nullable
    public N getRoot() {
        return root;
    }

    public void setRoot(@Nullable N root) {
        this.root = root;
        if (root != null) {
            root.makeRoot();
        }
    }

    @Nullable
    public N getLeftMost() {
        if (root == null) return null;
        return root.getLeftMost();
    }

    @Nullable
    public N getRightMost() {
        if (root == null) return null;
        return root.getRightMost();
    }

    private boolean isBlack(@Nullable N node) {
        return node == null || node.isBlack();
    }

    /**
     * Only assigns parent (or root if no parent), does not copy children
     */
    private void replaceWith(N node, @Nullable N replacement) {
        if (node.getParent() == null) {
            setRoot(replacement);
        } else {
            node.getParent().assign(node.getSide(), replacement);
        }
    }

    private void fixInsertion(N node) {
        assert node.isRed();

        N p = node.getParent();
        if (isBlack(p)) return; //adding red nodes to black parents is always fine

        if (p == root && p.isRed()) {
            p.setBlack(true);
            return;
        }

        N g = p.getParent();
        N u = p.getSibling(); //uncle
        assert g != null;
        if (isBlack(u)) {
            //g only has one red child p
            boolean nside = node.getSide();
            boolean pside = p.getSide();
            if (nside != pside) { //node is on the inside of p, relative to g
                rotate(p, !nside); //rotate node up to parent
            }

            //rotate black node g to make p the new black parent and recolor g to red
            rotate(g, !pside);
            //the node in place of g now has to red children
            return;
        }

        //grandparent has 2 red children
        //swap colors and propagate up.
        p.setBlack(true);
        u.setBlack(true);
        g.setRed(true);
        fixInsertion(g);
    }

    //recolouring and rotations for the case where
    //the node on the shortSide of p is black and is one short of the black height requirements of the tree
    //this implies that the node on the longSide of p has a blackHeight of at least 1
    private void fixRemoval(N p, boolean shortSide) {
        N s = p.getChild(!shortSide);
        assert isBlack(p.getChild(shortSide));
        assert s != null; //the long side must have nodes

        if (s.isRed()) {
            //if the sibling is red, it must have two black children (to have a black height > 0)
            //the parent must also be black
            //rotate so the parent becomes a red node on the short side
            rotate(p, shortSide);
            //reacquire new (black) sibling, previously inner nephew
            s = p.getChild(!shortSide);
            assert s != null;
            assert s.isBlack();
        }

        N oNeph = s.getChild(!shortSide);
        if (isBlack(oNeph)) {
            N iNeph = s.getChild(shortSide);
            if (isBlack(iNeph)) {
                //both nephews are black
                //color s red, making the black height equal, but short on both sides
                s.setRed(true);
                if (p.isRed()) {
                    p.setBlack(true); //just make p black, restoring desired black height
                } else if (p != root) {
                    fixRemoval(p.requireParent(), p.getSide());
                }
                return;
            }

            //inner nephew is red, rotate it up to s and change to black
            //change s (now a nephew) to red, all black heights preserved
            rotate(s, !shortSide);
            iNeph.setBlack(true);
            oNeph = s; // oNeph.setRed(true); (overwritten later)

            assert isBlack(p.getChild(!shortSide));
            assert oNeph == requireNonNull(p.getChild(!shortSide)).getChild(!shortSide);
        }

        //have a red outer nephew, rotate p to gain a black (s is black)
        //s takes the color of p, preserving height
        //outer nephew had a black parent s, which is now moved to the short side, oNeph is coloured black to compensate
        //inner nephew is moved to short side (which is no-longer short) and retains height
        rotate(p, shortSide);
        oNeph.setBlack(true);
    }

    private void rotate(N node, boolean right) {
        N inc = node.getChild(!right);
        assert inc != null; //can't rotate a null node into position

        replaceWith(node, inc);//move inc to node slot
        node.assign(!right, inc.getChild(right)); //pass orphan across
        inc.assign(right, node); //set node as child of inc

        if (node.isRed() != inc.isRed()) {
            node.setRed(!node.isRed());
            inc.setRed(!inc.isRed());
        }
        node.onChildrenChanged();
    }

    protected class Entries extends AbstractCollection<N> {

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof RedBlackNode)) return false;

            //TODO, this should just ignore nodes that arent owned by this tree instead of asserting?
            RedBlackNode<?> maybeNode = (RedBlackNode<?>) o;
            assert maybeNode.getRoot() == root;

            N node = unsafeCast(maybeNode);
            count--;
            _version++;

            N del = node;
            //If node has 2 children, identify successor and delete it instead
            if (node.getLeft() != null && node.getRight() != null) {
                del = node.getRight().getLeftMost();
            }

            assert del.getLeft() == null || del.getRight() == null; //node has at most one child
            N child = del.getLeft() == null ? del.getRight() : del.getLeft();

            //if deleted node is root
            if (del.getParent() == null) {
                setRoot(child);
                return true;
            }

            //save location of deleted node for tree fixing, because deleted node may be a successor instead
            boolean doubleBlack = del.isBlack() && isBlack(child);
            N deletedFrom = del.getParent();
            boolean deletedSide = del.getSide();
            replaceWith(del, child);
            if (child != null) {
                child.setBlack(true);
            }

            if (del != node) { //if deleted node was successor, replace the original node with the deleted successor
                replaceWith(node, del);
                del.setRed(node.isRed());
                del.setLeft(node.getLeft());
                del.setRight(node.getRight());

                if (deletedFrom == node) {
                    deletedFrom = del;
                }
            }

            if (deletedFrom != null) deletedFrom.onChildrenChanged();
            if (node.getParent() != null) node.getParent().onChildrenChanged();

            if (doubleBlack) {
                assert deletedFrom != null;
                fixRemoval(deletedFrom, deletedSide);
            }

            return true;
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Use ComparableRedBlackTree");
        }

        @Override
        public boolean add(N t) {
            throw new UnsupportedOperationException("Use ComparableRedBlackTree");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean r = false;
            for (Object o : c) {
                if (!(o instanceof RedBlackNode)) continue;
                RedBlackNode<?> node = (RedBlackNode<?>) o;
                if (node.getRoot() != root) continue;
                r |= remove(node);
            }
            return r;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Use ComparableRedBlackTree");
        }

        @Override
        public int size() {
            return count;
        }

        @Override
        public Iterator<N> iterator() {
            int v = _version;
            return new Iterator<N>() {
                @Nullable
                N n = getLeftMost();

                @Override
                public boolean hasNext() {
                    return n != null;
                }

                @Override
                public N next() {
                    assert n != null;//wat, bad iterator consumer.

                    if (v != _version) throw new ConcurrentModificationException();

                    N curr = n;
                    n = curr.getNext();
                    return curr;
                }
            };
        }

        @Override
        public void clear() {
            setRoot(null);
            count = 0;
            _version++;
        }
    }
}
