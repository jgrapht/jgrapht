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
    Node treeParent;
    Node firstTreeChild;
    Node treeSiblingNext;
    Node treeSiblingPrev;

    Node blossomParent;
    Node blossomGrandparent;
    Edge blossomSibling;

    int id;

    public Node() {
        this(false, false, true, false, PLUS);
    }

    public Node(Label label) {
        this(false, false, true, false, label);
    }

    public Node(boolean isTreeRoot, boolean isBlossom, boolean isOuter, boolean isProcessed, Label label) {
        this.isTreeRoot = isTreeRoot;
        this.isBlossom = isBlossom;
        this.isOuter = isOuter;
        this.isProcessed = isProcessed;
        this.label = label;
        this.first = new Edge[2];
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

    public void addChild(Node child) {
        child.treeParent = this;
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

    void removeFromChildList() {
        if (treeSiblingNext != null) {
            // this vertex isn't the last child
            treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
        } else {
            // this vertex is the last child
            if (treeParent != null && treeParent.firstTreeChild != null) {
                treeParent.firstTreeChild.treeSiblingPrev = treeSiblingPrev;
            }
        }
        if (treeSiblingPrev.treeSiblingNext != null) {
            // this vertex isn't the first child
            treeSiblingPrev.treeSiblingNext = treeSiblingNext;
        } else {
            // this vertex is the first child
            // we don't have to check whether treeParent is null since if this node
            // is root then treeSiblingPrev.treeSiblingNext != null
            treeParent.firstTreeChild = treeSiblingNext;
        }
    }

    void moveChildrenTo(Node blossom) {
        if (firstTreeChild != null) {
            if (blossom.firstTreeChild == null) {
                blossom.firstTreeChild = firstTreeChild;
            } else {
                Node first = firstTreeChild;
                first.treeSiblingPrev.treeSiblingNext = blossom.firstTreeChild;
                first.treeSiblingPrev = blossom.firstTreeChild.treeSiblingPrev;
                blossom.firstTreeChild.treeSiblingPrev = first.treeSiblingPrev;
                blossom.firstTreeChild = first;
            }
            firstTreeChild = null; // for debug purposes
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

    public boolean isInftyNode() {
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
        return "Node id = " + id;
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
