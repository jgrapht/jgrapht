/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.matching;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm. Represents an alternating
 * tree of tight edges which is used to find an augmenting path of tight edges in order to perform an augmentation
 * and increase the cardinality of the matching.
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class Tree {
    /**
     * Variable for debug purposes, todo: remove
     */
    private static int currentId = 1;
    /**
     * Two-element array of the first elements in the circular doubly linked lists of incident tree
     * edge in each direction.
     */
    TreeEdge[] first;
    /**
     * Used to quickly determine the edge between two trees during primal operations.
     */
    TreeEdge currentEdge;
    /**
     * Used to quickly determine the opposite tree during primal operations.
     */
    int currentDirection;
    /**
     * Stores the dual change that hasn't been spread among the nodes in this tree. This technique is called
     * lazy delta spreading
     */
    double eps;
    /**
     * Accumulates dual changes in the dual update phase
     */
    double accumulatedEps;
    /**
     * the root of this tree
     */
    Node root;
    /**
     * Supporting variable, used in updating duals via connected components
     */
    Tree nextTree;
    /**
     * The heap of (+,+) edges of this tree
     */
    FibonacciHeap<Edge> plusPlusEdges;
    /**
     * The heap of (+, inf) edges of this tree
     */
    FibonacciHeap<Edge> plusInfinityEdges;
    /**
     * The heap of "-" blossoms of this tree
     */
    FibonacciHeap<Node> minusBlossoms;
    /**
     * Variable for debug purposes, todo: remove
     */
    int id;

    /**
     * Empty constructor
     */
    public Tree() {
    }

    /**
     * Constructs a new tree with the {@code root}
     *
     * @param root the root of this tree
     */
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
        return "Tree id=" + id + ", eps = " + eps + ", root = " + root;
    }

    /**
     * Helper method to ensure correct addition of an edge to the heap
     *
     * @param edge a (+, +) edge
     * @param key  edge's key in the heap
     */
    public void addPlusPlusEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        plusPlusEdges.insert(edgeNode, key);
    }

    /**
     * Helper method to ensure correct addition of an edge to the heap
     *
     * @param edge a (+, inf) edge
     * @param key  edge's key in the heap
     */
    public void addPlusInfinityEdge(Edge edge, double key) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        plusInfinityEdges.insert(edgeNode, key);
    }

    /**
     * Helper method to ensure correct addition of a blossom to the heap
     *
     * @param blossom a "-" blossom
     * @param key     blossom's key in the heap
     */
    public void addMinusBlossom(Node blossom, double key) {
        FibonacciHeapNode<Node> blossomNode = new FibonacciHeapNode<>(blossom);
        blossom.fibNode = blossomNode;
        minusBlossoms.insert(blossomNode, key);
    }

    /**
     * Removes the {@code edge} from the heap of (+, +) edges
     *
     * @param edge the edge to remove
     */
    public void removePlusPlusEdge(Edge edge) {
        plusPlusEdges.delete(edge.fibNode);
        edge.fibNode = null; // strict mode, todo: remove
    }

    /**
     * Removes the {@code edge} from the heap of (+, inf) edges
     *
     * @param edge the edge to remove
     */
    public void removePlusInfinityEdge(Edge edge) {
        plusInfinityEdges.delete(edge.fibNode);
        edge.fibNode = null; // strict mode, todo: remove
    }

    /**
     * Removes the {@code blossom} from the heap of "-" blossoms
     *
     * @param blossom the blossom to remove
     */
    public void removeMinusBlossom(Node blossom) {
        minusBlossoms.delete(blossom.fibNode);
        blossom.fibNode = null; // strict mode: todo: remove
    }

    /**
     * Returns a new instance of TreeNodeIterator for this tree
     *
     * @return new TreeNodeIterator for this tree
     */
    public TreeNodeIterator treeNodeIterator() {
        return new TreeNodeIterator();
    }

    /**
     * Returns a new instance of TreeEdgeIterator for this tree
     *
     * @return new TreeEdgeIterators for this tree
     */
    public TreeEdgeIterator treeEdgeIterator() {
        return new TreeEdgeIterator();
    }

    /**
     * An iterator over tree edges incident to this tree.
     */
    public class TreeEdgeIterator implements Iterator<TreeEdge> {
        /**
         * The direction of the {@code currentEdge}
         */
        private int currentDirection;
        /**
         * The tree edge this iterator is currently on
         */
        private TreeEdge currentEdge;
        /**
         * Auxiliary variable to determine whether currentEdge has been returned or not
         */
        private TreeEdge result;

        /**
         * Constructs a new TreeEdgeIterator
         */
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

        @Override
        public TreeEdge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            TreeEdge res = result;
            result = null;
            return res;
        }

        /**
         * Returns the direction of the current edge
         *
         * @return the direction of the current edge
         */
        public int getCurrentDirection() {
            return currentDirection;
        }

        /**
         * Moves this iterator to the next tree edge. If the last outgoing edge has been traversed,
         * changes the current direction to 1. If the the last incoming edge has been traversed,
         * sets {@code currentEdge} to null.
         *
         * @return the next tree edge or null if all edges have been traversed already
         */
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
    }

    /**
     * An iterator over tree nodes
     */
    public class TreeNodeIterator implements Iterator<Node> {
        /**
         * The node this iterator is currently on
         */
        private Node currentNode;
        /**
         * Support variable to determine whether {@code currentNode} has been returned or not
         */
        private Node current;
        /**
         * Stores next tree root with respect to the root of this tree
         */
        private Node stop;

        /**
         * Constructs a new TreeNodeIterator
         */
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

        /**
         * Advances the iterator to the next tree node
         *
         * @return the next tree node
         */
        private Node advance() {
            if (currentNode == null) {
                return null;
            } else if (currentNode.firstTreeChild != null) {
                // advance deeper
                return currentNode = currentNode.firstTreeChild;
            } else {
                // advance to the next unvisited sibling of the current node or
                // of some of its ancestors
                while (currentNode != root && currentNode.treeSiblingNext == null) {
                    currentNode = currentNode.parentEdge.getOpposite(currentNode);
                }
                currentNode = currentNode.treeSiblingNext;
                return currentNode == stop ? currentNode = null : currentNode;
            }
        }
    }
}
