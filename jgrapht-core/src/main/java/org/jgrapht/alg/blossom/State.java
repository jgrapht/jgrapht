package org.jgrapht.alg.blossom;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

class State<V, E> {
    Node[] nodes;
    Edge[] edges;
    Tree[] tree;
    int nodeNum;
    int edgeNum;
    int treeNum;
    Map<V, Node> vertexMap;
    Map<E, Edge> edgeMap;

    public State(Node[] nodes, Edge[] edges, Tree[] tree, int nodeNum, int edgeNum, int treeNum, Map<V, Node> vertexMap, Map<E, Edge> edgeMap) {
        this.nodes = nodes;
        this.edges = edges;
        this.tree = tree;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.vertexMap = vertexMap;
        this.edgeMap = edgeMap;
    }

    public TreeRootsIterator treeRootsIterator() {
        return new TreeRootsIterator();
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
}
