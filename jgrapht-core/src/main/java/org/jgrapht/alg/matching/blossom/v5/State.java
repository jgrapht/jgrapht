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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores data needed for the Kolmogorov's Blossom V algorithm is used by {@link KolmogorovMinimumWeightPerfectMatching},
 * {@link PrimalUpdater} and {@link DualUpdater} during the course of the algorithm.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @see PrimalUpdater
 * @see DualUpdater
 * @since June 2018
 */
class State<V, E> {
    /**
     * Number of nodes in the graph
     */
    final int nodeNum;
    /**
     * Number of edges in the graph
     */
    final int edgeNum;
    /**
     * The graph to search matching in
     */
    Graph<V, E> graph;
    /**
     * An array of nodes of the graph.
     * Node: the size of the array is nodeNum + 1. The node nodes[nodeNum] is an auxiliary node that is used
     * as the first element in the linked list of tree roots
     */
    Node[] nodes;
    /**
     * An array of edges of the graph
     */
    Edge[] edges;
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
     * Options used to determine the strategies used in the algorithm
     */
    KolmogorovMinimumWeightPerfectMatching.Options options;
    /**
     * Mapping from initial vertices to nodes
     */
    Map<V, Node> vertexMap;
    /**
     * Mapping from nodes to corresponding initial vertices
     */
    Map<Node, V> backVertexMap;
    /**
     * Mapping from initial edges to the edges used in the algorithm
     */
    Map<E, Edge> edgeMap;
    /**
     * Mapping from edges used in the algorithm to the initial edges
     */
    Map<Edge, E> backEdgeMap;

    /**
     * Constructs an initial algorithm's state
     *
     * @param graph     the graph to search matching in
     * @param nodes     nodes used in the algorithm
     * @param edges     edges used in the algorithm
     * @param nodeNum   number of nodes in the graph
     * @param edgeNum   number of edges in the graph
     * @param treeNum   number of trees in the graph
     * @param vertexMap the map from initial graph's vertices to node
     * @param edgeMap   the map from initial graph's edges to the edges used in the algorithm
     * @param options   default or user defined options
     */
    public State(Graph<V, E> graph, Node[] nodes, Edge[] edges,
                 int nodeNum, int edgeNum, int treeNum,
                 Map<V, Node> vertexMap, Map<E, Edge> edgeMap, KolmogorovMinimumWeightPerfectMatching.Options options) {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.vertexMap = vertexMap;
        this.edgeMap = edgeMap;
        this.options = options;
        this.statistics = new KolmogorovMinimumWeightPerfectMatching.Statistics();
        backEdgeMap = new HashMap<>();
        backVertexMap = new HashMap<>();
        for (Map.Entry<E, Edge> entry : edgeMap.entrySet()) {
            backEdgeMap.put(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<V, Node> entry : vertexMap.entrySet()) {
            backVertexMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Adds a new tree edge from {@code from} to {@code to}. Sets the to.currentEdge and to.currentDirection
     * with respect to the tree {@code from}
     *
     * @param from the tail of the directed tree edge
     * @param to   the head of the directed tree edge
     */
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

    /**
     * Adds a new edge between {@code from} and {@code to}. The resulting edge points from {@code from} \
     * to {@code to}
     *
     * @param from  the tail of this edge
     * @param to    the head of this edge
     * @param slack the slack of the resulting edge
     * @return the newly added edge
     */
    public static Edge addEdge(Node from, Node to, double slack) {
        Edge edge = new Edge();
        edge.slack = slack;
        edge.headOriginal[0] = to;
        edge.headOriginal[1] = from;
        // the call to the Node#addEdge implies setting head[dir] reference
        from.addEdge(edge, 0);
        to.addEdge(edge, 1);
        return edge;
    }

    /**
     * Method for debug purposes. Prints all the nodes of the {@code tree}
     *
     * @param tree the tree whose nodes will be printed
     */
    public static void printTreeNodes(Tree tree) {
        System.out.println("Printing tree nodes");
        for (Tree.TreeNodeIterator iterator = tree.treeNodeIterator(); iterator.hasNext(); ) {
            System.out.println(iterator.next());
        }
    }

    /**
     * Method for debug purposes. Prints {@code blossomNode} and all its blossom siblings
     *
     * @param blossomNode the node to start from
     */
    public static void printBlossomNodes(Node blossomNode) {
        System.out.println("Printing blossom nodes");
        Node current = blossomNode;
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
    public void setCurrentEdges(Tree tree) {
        TreeEdge treeEdge;
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            treeEdge = iterator.next();
            Tree opposite = treeEdge.head[iterator.getCurrentDirection()];
            opposite.currentEdge = treeEdge;
            opposite.currentDirection = iterator.getCurrentDirection();
        }
    }

    /**
     * Clears the currentEdge variable of all adjacent to the {@code tree} trees
     *
     * @param tree the tree whose adjacent trees' currentEdges variable is modified
     */
    public void clearCurrentEdges(Tree tree) {
        tree.currentEdge = null;
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
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
    public void moveEdgeTail(Node from, Node to, Edge edge) {
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
    public BlossomNodesIterator blossomNodesIterator(Node root, Edge blossomFormingEdge) {
        return new BlossomNodesIterator(root, blossomFormingEdge);
    }

    /**
     * A helper iterator, traverses all the nodes in the blossom. It starts from the endpoints of the
     * (+,+) edge and goes up to the blossom root. These two paths to the blossom root are called branches.
     * The branch of the blossomFormingEdge.head[0] has direction 0, another one has direction 1.
     * <p>
     * <b>Node:</b> the nodes returned by this iterator aren't consecutive
     * <p>
     * <b>Note:</b> this iterator must return the blossom root in the first branch, i.e. when the
     * direction if 0. This feature is needed to setup the blossomSibling references correctly
     */
    public class BlossomNodesIterator implements Iterator<Node> {
        /**
         * Blossom's root
         */
        private Node root;
        /**
         * The node this iterator is currently on
         */
        private Node currentNode;
        /**
         * Helper variable, is used to determine whether currentNode has been returned or not
         */
        private Node current;
        /**
         * The current direction of this iterator
         */
        private int currentDirection;
        /**
         * The (+, +) edge of the blossom
         */
        private Edge blossomFormingEdge;

        /**
         * Constructs a new BlossomNodeIterator for the {@code root} and {@code blossomFormingEdge}
         *
         * @param root               the root of the blossom (the node which isn't matched to another
         *                           node in the blossom)
         * @param blossomFormingEdge a (+, +) edge in the blossom
         */
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

        /**
         * @return the current direction of this iterator
         */
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

        /**
         * Advances this iterator to the next node in the blossom
         *
         * @return an unvisited node in the blossom
         */
        private Node advance() {
            if (currentNode == null) {
                return null;
            } else if (currentNode == root && currentDirection == 0) {
                // we have just traversed blossom's root and now start to traverse the second branch
                currentDirection = 1;
                currentNode = blossomFormingEdge.head[1];
                if (currentNode == root) {
                    return currentNode = null;
                }
                return currentNode;
            } else if (currentNode.getTreeParent() == root && currentDirection == 1) {
                // we have just finished traversing the blossom's nodes
                return currentNode = null;
            } else {
                return currentNode = currentNode.getTreeParent();
            }
        }
    }
}
