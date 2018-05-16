package org.jgrapht.alg.blossom;

import java.util.*;
import java.util.function.Consumer;

class State<V, E> {
    Node[] nodes;
    Edge[] edges;
    Tree[] trees;
    int nodeNum;
    int edgeNum;
    int treeNum;
    BlossomPerfectMatching.Statistics statistics;
    Map<V, Node> vertexMap;
    Map<E, Edge> edgeMap;

    public State(Node[] nodes, Edge[] edges, Tree[] trees,
                 int nodeNum, int edgeNum, int treeNum,
                 BlossomPerfectMatching.Statistics statistics,
                 Map<V, Node> vertexMap, Map<E, Edge> edgeMap) {
        this.nodes = nodes;
        this.edges = edges;
        this.trees = trees;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.statistics = statistics;
        this.vertexMap = vertexMap;
        this.edgeMap = edgeMap;
    }

    public static TreeEdge addTreeEdge(Tree from, Tree to) {
        TreeEdge treeEdge = new TreeEdge();

        treeEdge.head[0] = to;
        treeEdge.head[1] = from;

        if (from.first[0] != null) {
            from.first[0].prev[0] = treeEdge;
        }
        if (to.first[1] != null) {
            to.first[1].prev[1] = treeEdge;
        }

        treeEdge.next[0] = from.first[0];
        treeEdge.next[1] = to.first[1];

        from.first[0] = treeEdge;
        to.first[1] = treeEdge;

        to.currentEdge = treeEdge;
        to.currentDirection = 0;
        return treeEdge;
    }

    public static Edge addEdge(Node from, Node to, double slack) {
        Edge edge = new Edge();
        edge.slack = slack;
        edge.headOriginal[0] = to;
        edge.headOriginal[1] = from;
        from.addEdge(edge, 0);
        to.addEdge(edge, 1);
        return edge;
    }

    public void moveEdge(Node from, Node to, Edge edge) {
        int dir = edge.getDirFrom(from);
        from.removeEdge(edge, dir);
        to.addEdge(edge, dir);
    }

    public TreeRootsIterator treeRootsIterator() {
        return new TreeRootsIterator();
    }

    public BlossomNodesIterator blossomNodesIterator(Node root, Edge blossomFormingEdge) {
        return new BlossomNodesIterator(root, blossomFormingEdge);
    }

    // debug only
    public Set<Node> treeRoots() {
        Set<Node> roots = new HashSet<>(treeNum);
        forEachTreeRoot(roots::add);
        return roots;
    }

    void forEachNode(Consumer<Node> action) {
        for (int i = 0; i < nodeNum; i++) {
            action.accept(nodes[i]);
        }
    }

    void forEachEdge(Consumer<Edge> action) {
        for (int i = 0; i < edgeNum; i++) {
            action.accept(edges[i]);
        }
    }

    void forEachTreeRoot(Consumer<Node> action) {
        for (Node root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            action.accept(root);
        }
    }

    public class TreeRootsIterator implements Iterator<Node> {
        private Node currentNode;
        private Node current;

        public TreeRootsIterator() {
            current = currentNode = nodes[nodeNum].treeSiblingNext;
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
        public Node next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node res = current;
            current = null;
            return res;
        }

        private Node advance() {
            return currentNode == null ? null : (currentNode = currentNode.treeSiblingNext);
        }
    }

    public class BlossomNodesIterator implements Iterator<Node> {
        private Node root;
        private Node currentNode;
        private Node current;
        private int currentDirection;
        private Edge blossomFormingEdge;

        public BlossomNodesIterator(Node root, Edge blossomFormingEdge) {
            this.root = root;
            this.blossomFormingEdge = blossomFormingEdge;
            currentNode = current = blossomFormingEdge.head[0];
            currentDirection = 0;
        }

        @Override
        public boolean hasNext() {
            if (current != null) {
                return true;
            }
            current = advance();
            return current != null;
        }

        public int getCurrentDirection() {
            return currentDirection;
        }

        @Override
        public Node next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node result = current;
            current = null;
            return result;
        }

        private Node advance() {
            if (currentNode == null) {
                return null;
            } else if (currentNode == root && currentDirection == 0) {
                currentDirection = 1;
                currentNode = blossomFormingEdge.head[1];
                if (currentNode == root) {
                    return currentNode = null;
                }
                return currentNode;
            } else if (currentNode.treeParent == root && currentDirection == 1) {
                return currentNode = null;
            } else {
                return currentNode = currentNode.treeParent;
            }
        }

    }
}
