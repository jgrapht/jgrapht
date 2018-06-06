package org.jgrapht.alg.blossom;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.jgrapht.alg.blossom.Node.Label.*;

class Node implements Iterable<Edge> {
    private static int currentId = 0;
    FibonacciHeapNode<Node> fibNode;

    boolean isTreeRoot;
    boolean isBlossom;
    boolean isOuter;
    boolean isProcessed;
    boolean isMarked;
    boolean isRemoved;

    Label label;
    Edge[] first;
    double dual;
    double blossomEps;

    Edge parentEdge;
    Edge matched;

    Tree tree;
    Node firstTreeChild;
    Node treeSiblingNext;
    Node treeSiblingPrev;

    Node blossomParent;
    Node blossomGrandparent;
    Edge blossomSibling;

    int id;

    public Node() {
        this.first = new Edge[2];
        this.label = PLUS;
        this.id = currentId++;
    }

    public void addEdge(Edge edge, int dir) {
        if (first[dir] == null) {
            first[dir] = edge.next[dir] = edge.prev[dir] = edge;
        } else {
            edge.prev[dir] = first[dir].prev[dir];
            edge.next[dir] = first[dir];
            first[dir].prev[dir].next[dir] = edge;
            first[dir].prev[dir] = edge;
        }
        edge.head[1 - dir] = this;
    }

    public Set<Pair<Edge, Integer>> getEdges() {
        Set<Pair<Edge, Integer>> edges = new HashSet<>();
        for (AdjacentEdgeIterator iterator = adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            edges.add(new Pair<>(edge, iterator.getDir()));
        }
        return edges;
    }

    public void removeEdge(Edge edge, int dir) {
        if (edge.prev[dir] == edge) {
            // its the only edge of this node in the direction dir
            first[dir] = null;
        } else {
            // remove edge from the linked list
            edge.prev[dir].next[dir] = edge.next[dir];
            edge.next[dir].prev[dir] = edge.prev[dir];
            first[dir] = edge.next[dir]; // avoid checking whether edge is the first edge of this node in the direction dir
        }
    }

    public Node getTreeGrandparent() {
        Node t = parentEdge.getOpposite(this);
        return t.parentEdge.getOpposite(t);
    }

    public Node getTreeParent() {
        return parentEdge == null ? null : parentEdge.getOpposite(this);
    }

    public void addChild(Node child, Edge parentEdge) {
        child.parentEdge = parentEdge;
        child.tree = tree;
        child.treeSiblingNext = firstTreeChild;
        if (firstTreeChild == null) {
            child.treeSiblingPrev = child;
        } else {
            child.treeSiblingPrev = firstTreeChild.treeSiblingPrev;
            firstTreeChild.treeSiblingPrev = child;
        }
        firstTreeChild = child;
    }

    /**
     * If this node is a tree root then removes this nodes from tree roots linked list.
     * Otherwise, removes this vertex from the linked list of tree children and updates
     * parent.firstTreeChild accordingly.
     */
    public void removeFromChildList() {
        if (isTreeRoot) {
            treeSiblingPrev.treeSiblingNext = treeSiblingNext;
            if (treeSiblingNext != null) {
                treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
            }
        } else {
            if (treeSiblingPrev.treeSiblingNext == null) {
                // this vertex is the first child => we have to update parent.firstTreeChild
                parentEdge.getOpposite(this).firstTreeChild = treeSiblingNext;
            } else {
                treeSiblingPrev.treeSiblingNext = treeSiblingNext;
            }
            if (treeSiblingNext == null) {
                // this vertex is the last child => we have to set treeSiblingPrev of the firstChild
                if (parentEdge.getOpposite(this).firstTreeChild != null) {
                    parentEdge.getOpposite(this).firstTreeChild.treeSiblingPrev = treeSiblingPrev;
                }
            } else {
                treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
            }
        }
    }

    /**
     * Appends the child list of this node to the beginning of the child of the {@code blossom} node.
     *
     * @param blossom the node to which the children of current node are moved
     */
    public void moveChildrenTo(Node blossom) {
        if (firstTreeChild != null) {
            if (blossom.firstTreeChild == null) {
                blossom.firstTreeChild = firstTreeChild;
            } else {
                Node first = firstTreeChild;
                Node t = blossom.firstTreeChild.treeSiblingPrev;
                // concatenating child lists
                first.treeSiblingPrev.treeSiblingNext = blossom.firstTreeChild;
                blossom.firstTreeChild.treeSiblingPrev = first.treeSiblingPrev;
                // setting reference to the last child and updating firstTreeChild reference of the blossom
                first.treeSiblingPrev = t;
                blossom.firstTreeChild = first;
            }
            firstTreeChild = null;
        }
    }

    public Node getPenultimateBlossom() {
        if (blossomParent == null) {
            return null;
        } else {
            Node current = this;
            while (true) {
                if (!current.blossomGrandparent.isOuter) {
                    current = current.blossomGrandparent;
                } else if (current.blossomGrandparent != current.blossomParent) {
                    // we make this to apply path compression for the latest shrinks
                    current.blossomGrandparent = current.blossomParent;
                } else {
                    break;
                }
            }
            // do path compression
            Node prev = this;
            Node next;
            while (prev != current) {
                next = prev.blossomGrandparent;
                prev.blossomGrandparent = current;
                prev = next;
            }

            return current;
        }

    }

    public boolean isPlusNode() {
        return label == PLUS;
    }

    public boolean isMinusNode() {
        return label == MINUS;
    }

    public boolean isInfinityNode() {
        return label == INFTY;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void forAllEdges(BiConsumer<Edge, Integer> action) {
        for (AdjacentEdgeIterator iterator = adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            action.accept(edge, iterator.getDir());
        }
    }

    public double getTrueDual() {
        if (tree == null || !isOuter) {
            return dual;
        }
        return isPlusNode() ? dual + tree.eps : dual - tree.eps;
    }

    @Override
    public Iterator<Edge> iterator() {
        return new AdjacentEdgeIterator();
    }

    public AdjacentEdgeIterator adjacentEdgesIterator() {
        return new AdjacentEdgeIterator();
    }

    @Override
    public String toString() {
        return "Node id = " + id + ", dual: " + dual + ", true dual: " + getTrueDual()
                + ", label: " + label + (isMarked ? ", marked" : "") + (isProcessed ? ", processed" : "");
    }

    enum Label {
        PLUS, MINUS, INFTY;
    }

    class AdjacentEdgeIterator implements Iterator<Edge> {

        private int dir;
        private Edge currentEdge;
        private Edge current;

        public AdjacentEdgeIterator() {
            if (first[0] == null) {
                this.currentEdge = first[1];
                this.dir = 1;
            } else {
                this.currentEdge = first[0];
                this.dir = 0;
            }
            current = currentEdge;
        }

        public int getDir() {
            return dir;
        }

        @Override
        public boolean hasNext() {
            if (current != null) {
                return true;
            }
            current = advance();
            return current != null;
        }

        @Override
        public Edge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Edge result = current;
            current = null;
            return result;
        }

        private Edge advance() {
            if (currentEdge == null) {
                return null;
            }
            currentEdge = currentEdge.next[dir];
            if (currentEdge == first[0]) {
                dir = 1;
                return currentEdge = first[1];
            } else if (currentEdge == first[1]) {
                return currentEdge = null;
            } else {
                return currentEdge;
            }
        }
    }
}
