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

import org.jheaps.MergeableAddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a supporting data structure for Kolmogorov's Blossom V algorithm.
 * <p>
 * Represents an alternating tree of <em>tight</em> edges which is used to find an augmenting path of tight edges
 * in order to perform an augmentation and increase the cardinality of the matching. The nodes on odd layers
 * are connected to their children necessarily via matched edges. Thus, these nodes have always exatly one child.
 * The nodes on even layers can have arbitrarily many children.
 * <p>
 * The tree structure information is contained in {@link BlossomVNode}, this class only contains the reference
 * to the root of the tree. It also contains three heaps:
 * <ul>
 * <li>A heap of (+, inf) edges. These edges are also called infinity edges. If there exist a tight
 * infinity edge, then it can be grown. Thus, this heap is used to determine an infinity edge of
 * minimum slack.</li>
 * <li>A heap of (+, +) in-tree edges. These are edges between "+" nodes from the same tree. If a (+, +)
 * in-tree edges is tight, it can be used to perform the shrink operation and introduce a new blossom. Thus,
 * this heap is used to determine a (+, +) in-tree edge of minimum slack in a given tree.</li>
 * <li>A heap of "-" blossoms. If there exists a blossom with zero actual dual variable, it can be expanded.
 * Thus, this heap is used to determine a "-" blossom with minimum dual variable</li>
 * </ul>
 * <p>
 * Each tree contains a variable which accumulated dual change applied to it. The dual changes aren't spread until
 * a tree is destroyed by an augmentation. For every node in the tree its true dual variable is equal to
 * {@code node.dual + node.tree.eps} if it is a "+" node, or it equals {@code node.dual - node.tree.eps}. This applies
 * only to the surface nodes that belong to some tree.
 * <p>
 * This class also contains implementations of two iterators: {@link TreeEdgeIterator} and {@link TreeNodeIterator}.
 * They are used to conveniently traverse the tree edges incident to a particular tree, and to traverse the nodes
 * of a tree in a depth-first order.
 *
 * @author Timofey Chudakov
 * @see BlossomVNode
 * @see BlossomVTreeEdge
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class BlossomVTree {
    /**
     * Variable for debug purposes, todo: remove
     */
    private static int currentId = 1;
    /**
     * Two-element array of the first elements in the circular doubly linked lists of incident tree
     * edges in each direction.
     */
    BlossomVTreeEdge[] first;
    /**
     * This variable is used to quickly determine the edge between two trees during primal operations.
     * <p>
     * Let $T$ be a tree that is being processed in the main loop. For every tree $T'$ that is adjacent
     * to $T$ this variable is set to the {@code BlossomVTreeEdge} that connects both trees. This variable also
     * helps to indicate whether a pair of trees is adjacent or not. This variable is set to {@code null}
     * when no primal operation can be applied to the tree $T$.
     */
    BlossomVTreeEdge currentEdge;
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
     * The root of this tree
     */
    BlossomVNode root;
    /**
     * Supporting variable, used in updating duals via connected components
     */
    BlossomVTree nextTree;
    /**
     * The heap of (+,+) edges of this tree
     */
    MergeableAddressableHeap<Double, BlossomVEdge> plusPlusEdges;
    /**
     * The heap of (+, inf) edges of this tree
     */
    MergeableAddressableHeap<Double, BlossomVEdge> plusInfinityEdges;
    /**
     * The heap of "-" blossoms of this tree
     */
    MergeableAddressableHeap<Double, BlossomVNode> minusBlossoms;
    /**
     * Variable for debug purposes, todo: remove
     */
    int id;

    /**
     * Empty constructor
     */
    public BlossomVTree() {
    }

    /**
     * Constructs a new tree with the {@code root}
     *
     * @param root the root of this tree
     */
    public BlossomVTree(BlossomVNode root) {
        this.root = root;
        root.tree = this;
        root.isTreeRoot = true;
        first = new BlossomVTreeEdge[2];
        plusPlusEdges = new PairingHeap<>();
        plusInfinityEdges = new PairingHeap<>();
        minusBlossoms = new PairingHeap<>();
        this.id = currentId++;
    }

    @Override
    public String toString() {
        return "BlossomVTree pos=" + id + ", eps = " + eps + ", root = " + root;
    }

    /**
     * Helper method to ensure correct addition of an edge to the heap
     *
     * @param edge a (+, +) edge
     */
    public void addPlusPlusEdge(BlossomVEdge edge) {
        edge.handle = plusPlusEdges.insert(edge.slack, edge);
    }

    /**
     * Helper method to ensure correct addition of an edge to the heap
     *
     * @param edge a (+, inf) edge
     */
    public void addPlusInfinityEdge(BlossomVEdge edge) {
        edge.handle = plusInfinityEdges.insert(edge.slack, edge);
    }

    /**
     * Helper method to ensure correct addition of a blossom to the heap
     *
     * @param blossom a "-" blossom
     */
    public void addMinusBlossom(BlossomVNode blossom) {
        blossom.handle = minusBlossoms.insert(blossom.dual, blossom);
    }

    /**
     * Removes the {@code edge} from the heap of (+, +) edges
     *
     * @param edge the edge to remove
     */
    public void removePlusPlusEdge(BlossomVEdge edge) {
        edge.handle.delete();
        edge.handle = null; // strict mode, todo: remove
    }

    /**
     * Removes the {@code edge} from the heap of (+, inf) edges
     *
     * @param edge the edge to remove
     */
    public void removePlusInfinityEdge(BlossomVEdge edge) {
        edge.handle.delete();
        edge.handle = null; // strict mode, todo: remove
    }

    /**
     * Removes the {@code blossom} from the heap of "-" blossoms
     *
     * @param blossom the blossom to remove
     */
    public void removeMinusBlossom(BlossomVNode blossom) {
        blossom.handle.delete();
        blossom.handle = null; // strict mode: todo: remove
    }

    /**
     * Returns a new instance of TreeNodeIterator for this tree
     *
     * @return new TreeNodeIterator for this tree
     */
    public TreeNodeIterator treeNodeIterator() {
        return new TreeNodeIterator(root);
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
     * An iterator over tree nodes. This iterator traverses the nodes of the tree in a depth-first order.
     * Note: this iterator can be used to iterate the nodes of some subtree a of a tree.
     */
    public static class TreeNodeIterator implements Iterator<BlossomVNode> {
        /**
         * The node this iterator is currently on
         */
        private BlossomVNode currentNode;
        /**
         * Support variable to determine whether {@code currentNode} has been returned or not
         */
        private BlossomVNode current;
        /**
         * Stores next tree root with respect to the root of this tree
         */
        private BlossomVNode treeRoot;

        /**
         * Constructs a new TreeNodeIterator for a {@code root}.
         * <p>
         * <b>Note:</b> {@code root} doesn't need to be a root of some tree, this iterator also
         * works with subtrees.
         *
         * @param root node of a tree to start dfs traversal from.
         */
        public TreeNodeIterator(BlossomVNode root) {
            this.currentNode = this.current = root;
            this.treeRoot = root;
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
        public BlossomVNode next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BlossomVNode result = current;
            current = null;
            return result;
        }

        /**
         * Advances the iterator to the next tree node
         *
         * @return the next tree node
         */
        private BlossomVNode advance() {
            if (currentNode == null) {
                return null;
            } else if (currentNode.firstTreeChild != null) {
                // advance deeper
                currentNode = currentNode.firstTreeChild;
                return currentNode;
            } else {
                // advance to the next unvisited sibling of the current node or
                // of some of its ancestors
                while (currentNode != treeRoot && currentNode.treeSiblingNext == null) {
                    currentNode = currentNode.parentEdge.getOpposite(currentNode);
                }
                currentNode = currentNode.treeSiblingNext;
                if(currentNode == treeRoot.treeSiblingNext){
                    currentNode = null;
                }
                return currentNode;
            }
        }
    }

    /**
     * An iterator over tree edges incident to this tree.
     */
    public class TreeEdgeIterator implements Iterator<BlossomVTreeEdge> {
        /**
         * The direction of the {@code currentEdge}
         */
        private int currentDirection;
        /**
         * The tree edge this iterator is currently on
         */
        private BlossomVTreeEdge currentEdge;
        /**
         * Auxiliary variable to determine whether currentEdge has been returned or not
         */
        private BlossomVTreeEdge result;

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
        public BlossomVTreeEdge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BlossomVTreeEdge res = result;
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
        private BlossomVTreeEdge advance() {
            if (currentEdge == null) {
                return null;
            }
            currentEdge = currentEdge.next[currentDirection];
            if (currentEdge == null && currentDirection == 0) {
                currentDirection = 1;
                currentEdge = first[1];
            }
            return currentEdge;
        }
    }
}
