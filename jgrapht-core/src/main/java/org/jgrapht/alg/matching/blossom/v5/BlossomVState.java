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

import org.jgrapht.Graph;

import java.util.*;

/**
 * Stores data needed for the Kolmogorov's Blossom V algorithm; it is used by {@link KolmogorovMinimumWeightPerfectMatching},
 * {@link BlossomVPrimalUpdater} and {@link BlossomVDualUpdater} during the course of the algorithm.
 * <p>
 * We refer to this object with all the data stores in nodes, edges, trees, and tree edges as the state of the algorithm
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @see BlossomVPrimalUpdater
 * @see BlossomVDualUpdater
 * @since June 2018
 */
class BlossomVState<V, E> {
    /**
     * Number of nodes in the graph
     */
    final int nodeNum;
    /**
     * Number of edges in the graph
     */
    final int edgeNum;
    /**
     * The graph for which to find a matching
     */
    Graph<V, E> graph;
    /**
     * An array of nodes of the graph.
     * BlossomVNode: the size of the array is nodeNum + 1. The node nodes[nodeNum] is an auxiliary node that is used
     * as the first element in the linked list of tree roots
     */
    BlossomVNode[] nodes;
    /**
     * An array of edges of the graph
     */
    BlossomVEdge[] edges;
    /**
     * Helper variable to determine whether an augmentation has been performed
     */
    int treeNum;
    /**
     * Number of expanded blossoms
     */
    int removedNum;
    /**
     * Number of blossoms
     */
    int blossomNum;
    /**
     * Statistics of the algorithm performance
     */
    KolmogorovMinimumWeightPerfectMatching.Statistics statistics;
    /**
     * BlossomVOptions used to determine the strategies used in the algorithm
     */
    BlossomVOptions options;
    /**
     * Mapping from initial vertices to nodes
     */
    List<V> graphVertices;

    List<E> graphEdges;

    /**
     * Constructs the algorithm's initial state
     *
     * @param graph         the graph for which to find a matching
     * @param nodes         nodes used in the algorithm
     * @param edges         edges used in the algorithm
     * @param nodeNum       number of nodes in the graph
     * @param edgeNum       number of edges in the graph
     * @param treeNum       number of trees in the graph
     * @param graphVertices generic vertices of the {@code graph} in the same order as nodes in {@code nodes}
     * @param graphEdges    generic edges of the {@code graph} in the same order as edges in {@code edges}
     * @param options       default or user defined options
     */
    public BlossomVState(Graph<V, E> graph, BlossomVNode[] nodes, BlossomVEdge[] edges,
                         int nodeNum, int edgeNum, int treeNum,
                         List<V> graphVertices, List<E> graphEdges, BlossomVOptions options) {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.graphVertices = graphVertices;
        this.graphEdges = graphEdges;
        this.options = options;
        this.statistics = new KolmogorovMinimumWeightPerfectMatching.Statistics();
    }

    /**
     * Adds a new tree edge from {@code from} to {@code to}. Sets the to.currentEdge and to.currentDirection
     * with respect to the tree {@code from}
     *
     * @param from the tail of the directed tree edge
     * @param to   the head of the directed tree edge
     */
    public static BlossomVTreeEdge addTreeEdge(BlossomVTree from, BlossomVTree to) {
        BlossomVTreeEdge treeEdge = new BlossomVTreeEdge();

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

    /**
     * Method for debug purposes. Prints all the nodes of the {@code tree}
     *
     * @param tree the tree whose nodes will be printed
     */
    public static void printTreeNodes(BlossomVTree tree) {
        System.out.println("Printing tree nodes");
        for (BlossomVTree.TreeNodeIterator iterator = tree.treeNodeIterator(); iterator.hasNext(); ) {
            System.out.println(iterator.next());
        }
    }

    /**
     * Method for debug purposes. Prints {@code blossomNode} and all its blossom siblings
     *
     * @param blossomNode the node to start from
     */
    public static void printBlossomNodes(BlossomVNode blossomNode) {
        System.out.println("Printing blossom nodes");
        BlossomVNode current = blossomNode;
        do {
            System.out.println(current);
            current = current.blossomSibling.getOpposite(current);
        } while (current != blossomNode);
    }

    /**
     * Sets the currentEdge and currentDirection variables for all adjacent to the {@code tree} trees
     *
     * @param tree the tree whose adjacent trees' variables are modified
     */
    public void setCurrentEdges(BlossomVTree tree) {
        BlossomVTreeEdge treeEdge;
        for (BlossomVTree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            treeEdge = iterator.next();
            BlossomVTree opposite = treeEdge.head[iterator.getCurrentDirection()];
            opposite.currentEdge = treeEdge;
            opposite.currentDirection = iterator.getCurrentDirection();
        }
    }

    /**
     * Clears the currentEdge variable of all adjacent to the {@code tree} trees
     *
     * @param tree the tree whose adjacent trees' currentEdges variable is modified
     */
    public void clearCurrentEdges(BlossomVTree tree) {
        tree.currentEdge = null;
        for (BlossomVTree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            iterator.next().head[iterator.getCurrentDirection()].currentEdge = null;
        }
    }

    /**
     * Moves the tail of the {@code edge} from the node {@code from} to the node {@code to}
     *
     * @param from the previous edge's tail
     * @param to   the new edge's tail
     * @param edge the edge whose tail is being changed
     */
    public void moveEdgeTail(BlossomVNode from, BlossomVNode to, BlossomVEdge edge) {
        int dir = edge.getDirFrom(from);
        from.removeEdge(edge, dir);
        to.addEdge(edge, dir);
    }

    /**
     * Returns a new instance of blossom nodes iterator
     *
     * @param root               the root of the blossom
     * @param blossomFormingEdge the (+,+) edge in the blossom
     * @return a new instance of blossom nodes iterator
     */
    public BlossomNodesIterator blossomNodesIterator(BlossomVNode root, BlossomVEdge blossomFormingEdge) {
        return new BlossomNodesIterator(root, blossomFormingEdge);
    }

    /**
     * A helper iterator which traverses all the nodes in the blossom. It starts from the endpoints of the
     * (+,+) edge and goes up to the blossom root. These two paths to the blossom root are called branches.
     * The branch of the blossomFormingEdge.head[0] has direction 0, the one has direction 1.
     * <p>
     * <b>Note:</b> the nodes returned by this iterator aren't consecutive
     * <p>
     * <b>Note:</b> this iterator must return the blossom root in the first branch, i.e. when the
     * direction is 0. This feature is needed to setup the blossomSibling references correctly
     */
    public static class BlossomNodesIterator implements Iterator<BlossomVNode> {
        /**
         * Blossom's root
         */
        private BlossomVNode root;
        /**
         * The node this iterator is currently on
         */
        private BlossomVNode currentNode;
        /**
         * Helper variable, is used to determine whether currentNode has been returned or not
         */
        private BlossomVNode current;
        /**
         * The current direction of this iterator
         */
        private int currentDirection;
        /**
         * The (+, +) edge of the blossom
         */
        private BlossomVEdge blossomFormingEdge;

        /**
         * Constructs a new BlossomNodeIterator for the {@code root} and {@code blossomFormingEdge}
         *
         * @param root               the root of the blossom (the node which isn't matched to another
         *                           node in the blossom)
         * @param blossomFormingEdge a (+, +) edge in the blossom
         */
        public BlossomNodesIterator(BlossomVNode root, BlossomVEdge blossomFormingEdge) {
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

        /**
         * @return the current direction of this iterator
         */
        public int getCurrentDirection() {
            return currentDirection;
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
         * Advances this iterator to the next node in the blossom
         *
         * @return an unvisited node in the blossom
         */
        private BlossomVNode advance() {
            if (currentNode == null) {
                return null;
            }
            if (currentNode == root && currentDirection == 0) {
                // we have just traversed blossom's root and now start to traverse the second branch
                currentDirection = 1;
                currentNode = blossomFormingEdge.head[1];
                if (currentNode == root) {
                    currentNode = null;
                }
            } else if (currentNode.getTreeParent() == root && currentDirection == 1) {
                // we have just finished traversing the blossom's nodes
                currentNode = null;
            } else {
                currentNode = currentNode.getTreeParent();
            }
            return currentNode;
        }
    }
}
