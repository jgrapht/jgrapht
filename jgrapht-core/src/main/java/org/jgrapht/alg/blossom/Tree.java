package org.jgrapht.alg.blossom;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class Tree {
    private static int currentId = 1;
    TreeEdge[] first;
    TreeEdge currentEdge;
    int currentDirection;
    double eps;
    double accumulatedEps;  // accumulates dual changes in dual updates
    Node root;
    FibonacciHeap<Edge> plusPlusEdges;
    FibonacciHeap<Edge> plusInfinityEdges;
    FibonacciHeap<Node> minusBlossoms;
    private int id;

    public Tree(Node root) {
        this.root = root;
        root.tree = this;
        root.isTreeRoot = true;
        first = new TreeEdge[2];
        plusPlusEdges = new FibonacciHeap<>();
        plusInfinityEdges = new FibonacciHeap<>();
        minusBlossoms = new FibonacciHeap<>();
        this.id = currentId++;
    }

    @Override
    public String toString() {
        return "Tree id=" + id;
    }

    public FibonacciHeapNode<Edge> addInfinityEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        plusInfinityEdges.insert(edgeNode, key);
        return edgeNode;
    }

    public FibonacciHeapNode<Node> addMinusBlossom(Node blossom, double key) {
        FibonacciHeapNode<Node> blossomNode = new FibonacciHeapNode<>(blossom);
        blossom.fibNode = blossomNode;
        minusBlossoms.insert(blossomNode, key);
        return blossomNode;
    }

    public FibonacciHeapNode<Edge> addPlusPlusEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        plusPlusEdges.insert(edgeNode, key);
        return edgeNode;
    }

    public void removePlusInfinityEdge(Edge edge) {
        plusInfinityEdges.delete(edge.fibNode);
        edge.fibNode = null;
    }

    public void removeMinusBlossom(Node blossom) {
        minusBlossoms.delete(blossom.fibNode);
        blossom.fibNode = null;
    }

    public void removePlusPlusEdge(Edge edge) {
        plusPlusEdges.delete(edge.fibNode);
        edge.fibNode = null;
    }

    public void forEachTreeEdge(BiConsumer<TreeEdge, Integer> action) {
        for (TreeEdgeIterator iterator = treeEdgeIterator(); iterator.hasNext(); ) {
            action.accept(iterator.next(), iterator.getCurrentDirection());
        }
    }

    public void forEachTreeNode(Consumer<Node> action) {
        for (TreeNodeIterator iterator = treeNodeIterator(); iterator.hasNext(); ) {
            action.accept(iterator.next());
        }
    }

    public TreeNodeIterator treeNodeIterator() {
        return new TreeNodeIterator();
    }

    public TreeEdgeIterator treeEdgeIterator() {
        return new TreeEdgeIterator();
    }

    public class TreeEdgeIterator implements Iterator<TreeEdge> {
        private int currentDirection;
        private TreeEdge currentEdge;
        private TreeEdge result;

        public TreeEdgeIterator() {
            currentEdge = first[0];
            currentDirection = 0;
            if (currentEdge == null) {
                currentEdge = first[1];
                currentDirection = 1;
            }
            result = currentEdge;
        }

        @Override
        public boolean hasNext() {
            if (result != null) {
                return true;
            }
            result = advance();
            return result != null;
        }

        public int getCurrentDirection() {
            return currentDirection;
        }


        private TreeEdge advance() {
            if (currentEdge == null) {
                return null;
            } else {
                currentEdge = currentEdge.next[currentDirection];
                if (currentEdge == null && currentDirection == 0) {
                    currentDirection = 1;
                    currentEdge = first[1];
                }
                return currentEdge;
            }
        }

        @Override
        public TreeEdge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            TreeEdge res = result;
            result = null;
            return res;
        }
    }

    public class TreeNodeIterator implements Iterator<Node> {
        private Node currentNode;
        private Node current;
        private Node stop;

        public TreeNodeIterator() {
            this.currentNode = this.current = root;
            this.stop = root.treeSiblingNext;
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
            Node result = current;
            current = null;
            return result;
        }

        private Node advance() {
            if (currentNode == null) {
                return null;
            } else if (currentNode.firstTreeChild != null) {
                return currentNode = currentNode.firstTreeChild;
            } else {
                while (currentNode != root && currentNode.treeSiblingNext == null) {
                    currentNode = currentNode.parent;
                }
                currentNode = currentNode.treeSiblingNext;
                return currentNode == stop ? currentNode = null : currentNode;
            }
        }
    }
}
