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
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * Is used to maintain an auxiliary graph whose nodes correspond to alternating trees in the Blossom V algorithm.
 * Let's denote the current tree $T$ and some other tree $T'$. Every tree edge contains three heaps:<br>
 * <ol>
 * <li>a heap of (+, +) cross-tree edges. This heap contains all edges between two "+" nodes where one node
 * belongs to tree $T$ and another to $T'$. The (+, +) cross-tree edges are used to augment the matching.</li>
 * <li>a heap of (+, -) cross-tree edges</li>
 * <li>a heap of (-, +) cross-tree edges</li>
 * </ol>
 * Note, that from the tree edge prospective there is no difference between a heap of (+, -) and (-, +)
 * cross-tree edges. That's we distinguish these heaps by the direction of the edge. Here the direction is considered
 * with respect to the trees $T$ and $T'$ resting upon the notation introduced above.
 * <p>
 * Every tree edge is directed from one tree to another and every tree edge belongs to the two doubly
 * linked lists of tree edges. The presence of a tree edge in these lists in maintained by the two-element
 * arrays {@link TreeEdge#prev} and {@link TreeEdge#next}. For one tree the edge is an outgoing tree edge, for another -
 * an incoming. In the first case it belongs to the {@code tree.first[0]} linked list, in the second - to the
 * {@code tree.first[1]} linked list.
 * <p>
 * Let {@code tree} be a tail of the edge, {@code oppositeTree} - a head of the edge. Then
 * {@code edge.head[0] == oppositeTree} and {@code edge.head[1] == tree}.
 *
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @see Tree
 * @see Edge
 * @since June 2018
 */
class TreeEdge {
    /**
     * Debug variable
     */
    private static int ID = 0;
    /**
     * Two-element array of trees this edge is incident to.
     */
    Tree[] head;
    /**
     * A two-element array of references to the previous elements in the circular doubly linked lists of tree edges.
     * The lists are circular with one exception: the lastElement.next[dir] == null. Each list belongs to
     * one of the endpoints of this edge.
     */
    TreeEdge[] prev;
    /**
     * A two-element array of references to the next elements in the circular doubly linked lists of tree edges.
     * The lists are circular with one exception: the lastElementInTheList.next[dir] == null. Each list belongs to
     * one of the endpoints of this edge.
     */
    TreeEdge[] next;
    /**
     * A heap of (+, +) cross-tree edges
     */
    FibonacciHeap<Edge> plusPlusEdges;
    /**
     * A heap of (-, +) cross-tree edges
     */
    FibonacciHeap<Edge> plusMinusEdges0;
    /**
     * A heap of (+, -) cross-tree edges
     */
    FibonacciHeap<Edge> plusMinusEdges1;
    /**
     * Debug variable
     */
    private int id;

    /**
     * Constructs a new tree edge by initializing arrays and heaps
     */
    public TreeEdge() {
        this.head = new Tree[2];
        this.prev = new TreeEdge[2];
        this.next = new TreeEdge[2];
        this.plusPlusEdges = new FibonacciHeap<>();
        this.plusMinusEdges0 = new FibonacciHeap<>();
        this.plusMinusEdges1 = new FibonacciHeap<>();
        id = ID++;
    }

    /**
     * Removes this edge from both doubly linked lists of tree edges.
     */
    public void removeFromTreeEdgeList() {
        for (int dir = 0; dir < 2; dir++) {
            if (prev[dir] != null) {
                prev[dir].next[dir] = next[dir];
            } else {
                // this is the first edge in this direction
                head[1 - dir].first[dir] = next[dir];
            }
            if (next[dir] != null) {
                next[dir].prev[dir] = prev[dir];
            }
        }
        head[0] = head[1] = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TreeEdge (" + head[0].id + ":" + head[1].id + "), pos = " + id;
    }

    /**
     * Adds {@code edge} to the heap of (-, +) cross-tree edges. As explained in the class description, this method
     * chooses {@link TreeEdge#plusMinusEdges0} or {@link TreeEdge#plusMinusEdges1} resting upon the {@code direction}.
     * The key is edge.slack
     *
     * @param edge      an edge to add to the current heap of (-, +) cross-tree edges.
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void addToCurrentMinusPlusHeap(Edge edge, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentMinusPlusHeap(direction).insert(edgeNode, edge.slack);
    }

    /**
     * Adds {@code edge} to the heap of (+, -) cross-tree edges. As explained in the class description, this method
     * chooses {@link TreeEdge#plusMinusEdges0} or {@link TreeEdge#plusMinusEdges1} resting upon the {@code direction}.
     * The key is edge.slack
     *
     * @param edge      an edge to add to the current heap of (+, -) cross-tree edges.
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void addToCurrentPlusMinusHeap(Edge edge, int direction) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        getCurrentPlusMinusHeap(direction).insert(edgeNode, edge.slack);
    }

    /**
     * Adds {@code edge} to the heap of (+, +) cross-tree edges. The key is edge.slack
     *
     * @param edge an edge to add to the heap of (+, +) cross-tree edges
     */
    public void addPlusPlusEdge(Edge edge) {
        FibonacciHeapNode<Edge> edgeNode = new FibonacciHeapNode<>(edge);
        edge.fibNode = edgeNode;
        this.plusPlusEdges.insert(edgeNode, edge.slack);
    }

    /**
     * Removes {@code edge} from the current heap of (-, +) cross-tree edges. As explained in the
     * class description, this method chooses {@link TreeEdge#plusMinusEdges0} or {@link TreeEdge#plusMinusEdges1}
     * resting upon the {@code direction}.
     *
     * @param edge      an edge to remove
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void removeFromCurrentMinusPlusHeap(Edge edge, int direction) {
        getCurrentMinusPlusHeap(direction).delete(edge.fibNode);
        edge.fibNode = null;
    }

    /**
     * Removes {@code edge} from the current heap of (+, -) cross-tree edges. As explained in the
     * class description, this method chooses {@link TreeEdge#plusMinusEdges0} or {@link TreeEdge#plusMinusEdges1}
     * resting upon the {@code direction}.
     *
     * @param edge      an edge to remove
     * @param direction direction of this tree edge wrt. current tree and opposite tree
     */
    public void removeFromCurrentPlusMinusHeap(Edge edge, int direction) {
        getCurrentPlusMinusHeap(direction).delete(edge.fibNode);
        edge.fibNode = null;
    }

    /**
     * Removes {@code edge} from the heap of (+, +) cross-tree edges.
     *
     * @param edge an edge to remove
     */
    public void removeFromPlusPlusHeap(Edge edge) {
        plusPlusEdges.delete(edge.fibNode);
        edge.fibNode = null;
    }

    /**
     * Returns the current heap of (-, +) cross-tree edges. Always returns the different from
     * {@code gerCurrentPlusMinusHeap(currentDir)}
     *
     * @param currentDir the current direction of this edge
     * @return returns current heap of (-, +) cross-tree edges
     */
    public FibonacciHeap<Edge> getCurrentMinusPlusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges0 : plusMinusEdges1;
    }

    /**
     * Returns the current heap of (+, -) cross-tree edges. Always returns the different from
     * {@code gerCurrentMinusPlusHeap(currentDir)}
     *
     * @param currentDir the current direction of this edge
     * @return returns current heap of (+, -) cross-tree edges
     */
    public FibonacciHeap<Edge> getCurrentPlusMinusHeap(int currentDir) {
        return currentDir == 0 ? plusMinusEdges1 : plusMinusEdges0;
    }
}
