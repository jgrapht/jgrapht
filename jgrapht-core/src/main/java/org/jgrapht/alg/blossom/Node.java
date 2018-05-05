package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeapNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static org.jgrapht.alg.blossom.Node.Label.*;

class Node implements Comparable<Node>, Iterable<Edge> {
    private static int currentId = 1;
    FibonacciHeapNode<Node> fibNode;
    boolean isPseudonode;
    boolean isTreeRoot;
    boolean isBlossom;
    boolean isOuter;
    boolean isProcessed;
    Label label;
    Edge[] first;
    Edge matched;
    Tree tree;
    double dual;
    Node parent;
    Node firstTreeChild;
    Node treeSiblingNext;
    Node treeSiblingPrev;
    Node blossomParent;
    Node blossomGrandparent;
    private int id;

    public Node() {
        this(false, false, false, true, false, PLUS);
    }

    public Node(Label label) {
        this(false, false, false, true, false, label);
    }

    public Node(boolean isPseudonode, boolean isTreeRoot, boolean isBlossom, boolean isOuter, boolean isProcessed, Label label) {
        this.isPseudonode = isPseudonode;
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

    public void forAllEdges(BiConsumer<Edge, Integer> action) {
        Edge edge = first[0];
        if (edge != null) {
            action.accept(edge, 0);
            edge = edge.next[0];
            while (edge != first[0]) {
                action.accept(edge, 0);
                edge = edge.next[0];
            }
        }
        edge = first[1];
        if (edge != null) {
            action.accept(edge, 1);
            edge = edge.next[1];
            while (edge != first[1]) {
                action.accept(edge, 1);
                edge = edge.next[1];
            }
        }
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

    @Override
    public Iterator<Edge> iterator() {
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
