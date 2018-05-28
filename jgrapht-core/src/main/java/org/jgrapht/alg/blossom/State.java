package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;

import java.util.*;

class State<V, E> {
    Graph<V, E> graph;
    Node[] nodes;
    Edge[] edges;
    Tree[] trees;
    int nodeNum;
    int edgeNum;
    int treeNum;
    int removedNum;
    BlossomPerfectMatching.Statistics statistics;
    Map<V, Node> vertexMap;
    Map<E, Edge> edgeMap;
    Map<Edge, E> backEdgeMap;

    public State(Graph<V, E> graph, Node[] nodes, Edge[] edges, Tree[] trees,
                 int nodeNum, int edgeNum, int treeNum,
                 BlossomPerfectMatching.Statistics statistics,
                 Map<V, Node> vertexMap, Map<E, Edge> edgeMap) {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        this.trees = trees;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.statistics = statistics;
        this.vertexMap = vertexMap;
        this.edgeMap = edgeMap;
        backEdgeMap = new HashMap<>();
        for (Map.Entry<E, Edge> entry : edgeMap.entrySet()) {
            backEdgeMap.put(entry.getValue(), entry.getKey());
        }
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

    public void setCurrentEdges(Tree tree) {
        TreeEdge treeEdge;
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            treeEdge = iterator.next();
            Tree opposite = treeEdge.head[iterator.getCurrentDirection()];
            opposite.currentEdge = treeEdge;
            opposite.currentDirection = iterator.getCurrentDirection();
        }
    }

    public void clearCurrentEdges(Tree tree) {
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            iterator.next().head[iterator.getCurrentDirection()].currentEdge = null;
        }
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

    public NodesIterator nodesIterator() {
        return new NodesIterator();
    }

    public EdgesIterator edgesIterator() {
        return new EdgesIterator();
    }

    // debug only
    public Set<Node> treeRoots() {
        Set<Node> roots = new HashSet<>(treeNum);
        for (TreeRootsIterator iterator = treeRootsIterator(); iterator.hasNext(); ) {
            roots.add(iterator.next());
        }
        return roots;
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

    class NodesIterator implements Iterator<Node> {
        Node current = nodes[0];
        // TODO simplify
        private int pos = 0;

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
            Node result = current;
            current = null;
            return result;
        }

        private Node advance() {
            return ++pos < nodeNum ? (current = nodes[pos]) : null;
        }
    }

    public class EdgesIterator implements Iterator<Edge> {
        private int current = 0;

        @Override
        public boolean hasNext() {
            return edges.length > current;
        }

        @Override
        public Edge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return edges[current++];
        }

    }
}
