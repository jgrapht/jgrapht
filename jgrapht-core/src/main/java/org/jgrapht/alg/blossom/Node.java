package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeapNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static org.jgrapht.alg.blossom.Node.Label.*;

class Node implements Comparable<Node>, Iterable<Edge> {
    private static int currentId = 0;
    FibonacciHeapNode<Node> fibNode;
    boolean isTreeRoot;
    boolean isBlossom;
    boolean isOuter;
    boolean isProcessed;
    boolean isMarked;
    Label label;
    Edge[] first;
    Edge matched;
    Tree tree;
    double dual;
    Node parent;
    Edge parentEdge;
    Node firstTreeChild;
    Node treeSiblingNext;
    Node treeSiblingPrev;
    Node blossomParent;
    Node blossomGrandparent;
    private int id;

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

    static Edge addEdgeBetween(Node node1, Node node2) {
        return addEdgeBetween(node1, node2, 0);
    }

    static Edge addEdgeBetween(Node node1, Node node2, double slack) {
        Edge edge = new Edge(node1, node2, slack);
        node1.addEdge(edge, 0);
        node2.addEdge(edge, 1);
        return edge;
    }

    @Override
    public String toString() {
        return "Node id = " + id;
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

    public void removeEdge(Edge edge, int dir) {
        if (edge.prev[dir].next[dir] == edge) {
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
        child.parent = this;
        child.tree = tree;
        child.firstTreeChild = null;
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
        if (treeSiblingNext == null) {
            parent.firstTreeChild.treeSiblingPrev = treeSiblingPrev;
        } else {
            treeSiblingNext.treeSiblingPrev = treeSiblingPrev;
        }
        if (treeSiblingPrev.treeSiblingNext != null) {
            treeSiblingPrev.treeSiblingNext = treeSiblingNext;
        } else {
            parent.firstTreeChild = treeSiblingNext;
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

    @Override
    public int compareTo(Node o) {
        return Double.compare(dual, o.dual);
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {

        this.tree = tree;
    }

    public void forAllEdges(BiConsumer<Edge, Integer> action) {
        for (AdjacentEdgeIterator iterator = adjacentEdgesIterator(); iterator.hasNext(); ) {
            Edge edge = iterator.next();
            action.accept(edge, iterator.getDir());
        }
    }

    @Override
    public Iterator<Edge> iterator() {
        return new AdjacentEdgeIterator();
    }

    public AdjacentEdgeIterator adjacentEdgesIterator() {
        return new AdjacentEdgeIterator();
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
