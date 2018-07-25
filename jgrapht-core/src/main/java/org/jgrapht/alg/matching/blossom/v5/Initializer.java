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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching.INFINITY;
import static org.jgrapht.alg.matching.blossom.v5.Options.InitializationType.GREEDY;

/**
 * Is used to start the Kolmogorov's Blossom V algorithm.
 * Performs initialization of the algorithm's internal data structures and finds an initial matching
 * according to the strategy specified in {@code options}
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovMinimumWeightPerfectMatching
 * @since June 2018
 */
class Initializer<V, E> {
    /**
     * The graph to search matching in
     */
    private final Graph<V, E> graph;
    /**
     * Number of nodes in the graph
     */
    private int nodeNum = 0;
    /**
     * Number of edges in the graph
     */
    private int edgeNum = 0;
    /**
     * An array of nodes that will be passes to the resulting state object
     */
    private Node[] nodes;
    /**
     * An array of edges that will be passes to the resulting state object
     */
    private Edge[] edges;
    /**
     * Generic vertices of the {@code graph} in the same order as internal nodes in the array {@code nodes}.
     * Since for each node in the {@code nodes} we know its position in the {@code nodes}, we can in constant
     * time determine its generic counterpart.
     */
    private List<V> graphVertices;
    /**
     * Generic edge of the {@code graph} in the same order as internal edges in the array {@code edges}.
     * Since for each edge in the {@code edges} we know its position in the {@code edges}, we can in constant
     * time determine its generic counterpart.
     */
    private List<E> graphEdges;

    /**
     * Creates a new Initializer instance
     *
     * @param graph the graph to search matching in
     */
    public Initializer(Graph<V, E> graph) {
        this.graph = graph;
    }

    /**
     * Converts the generic graph representation into the data structure form convenient for the algorithm
     * and initializes the matching according to the strategy specified in {@code options}
     *
     * @param options the options of the algorithm
     * @return the state object with all necessary for the algorithm information
     */
    public State<V, E> initialize(Options options) {
        initGraph();
        int treeNum;
        Options.InitializationType type = options.initializationType;
        if (type == GREEDY) {
            treeNum = initGreedy();
        } else {
            // simple initialization
            treeNum = nodeNum;
            for (Node node : nodes) {
                node.isOuter = true;
            }
        }
        allocateTrees();
        // initializing tree edges and adding cross-tree edges to corresponding heaps
        initAuxiliaryGraph();
        return new State<>(graph, nodes, edges, nodeNum, edgeNum, treeNum, graphVertices, graphEdges, options);
    }

    /**
     * Helper method to convert the generic graph representation into the form convenient for the algorithm
     */
    private void initGraph() {
        nodeNum = graph.vertexSet().size();
        int expectedEdgeNum = graph.edgeSet().size();
        nodes = new Node[nodeNum + 1];
        edges = new Edge[expectedEdgeNum];
        graphVertices = new ArrayList<>(nodeNum);
        graphEdges = new ArrayList<>(expectedEdgeNum);
        HashMap<V, Node> vertexMap = new HashMap<>(nodeNum);
        int i = 0;
        // mapping nodes
        for (V vertex : graph.vertexSet()) {
            nodes[i] = new Node(i);
            graphVertices.add(vertex);
            vertexMap.put(vertex, nodes[i]);
            i++;
        }
        nodes[nodeNum] = new Node(nodeNum);  // auxiliary node to keep track of the first item in the linked list of tree roots
        i = 0;
        // mapping edges
        for (E e : graph.edgeSet()) {
            Node source = vertexMap.get(graph.getEdgeSource(e));
            Node target = vertexMap.get(graph.getEdgeTarget(e));
            if (source != target) { // we avoid self-loops in order to support pseudographs
                edgeNum++;
                Edge edge = addEdge(source, target, graph.getEdgeWeight(e), i);
                edges[i] = edge;
                graphEdges.add(e);
                i++;
            }
        }
    }

    /**
     * Adds a new edge between {@code from} and {@code to}. The resulting edge points from {@code from} \
     * to {@code to}
     *
     * @param from  the tail of this edge
     * @param to    the head of this edge
     * @param slack the slack of the resulting edge
     * @param pos position of the resulting edge in the array {@code edges}
     * @return the newly added edge
     */
    public Edge addEdge(Node from, Node to, double slack, int pos) {
        Edge edge = new Edge(pos);
        edge.slack = slack;
        edge.headOriginal[0] = to;
        edge.headOriginal[1] = from;
        // the call to the Node#addEdge implies setting head[dir] reference
        from.addEdge(edge, 0);
        to.addEdge(edge, 1);
        return edge;
    }

    /**
     * Method for greedy matching initialization.
     * <p>
     * For every node we choose an incident edge of minimum slack and set its dual to the half of this slack.
     * This maintains the nonnegativity of edge slacks. After that we go through all nodes again, greedily
     * increase their dual variables and match them if it is possible.
     *
     * @return the number of unmatched nodes, which equals to the number of trees
     */
    private int initGreedy() {
        int dir;
        Edge edge;
        // set all dual variables to infinity
        for (int i = 0; i < nodeNum; i++) {
            nodes[i].dual = INFINITY;
        }
        // set dual variables to the half of the minimum weight of the incident edges
        for (int i = 0; i < edgeNum; i++) {
            edge = edges[i];
            if (edge.head[0].dual > edge.slack) {
                edge.head[0].dual = edge.slack;
            }
            if (edge.head[1].dual > edge.slack) {
                edge.head[1].dual = edge.slack;
            }
        }
        // divide dual variables by to, this ensures nonnegativity of all slacks
        // decrease edge slacks accordingly
        for (int i = 0; i < edgeNum; i++) {
            edge = edges[i];
            Node source = edge.head[0];
            Node target = edge.head[1];
            if (!source.isOuter) {
                source.isOuter = true;
                source.dual /= 2;
            }
            edge.slack -= source.dual;
            if (!target.isOuter) {
                target.isOuter = true;
                target.dual /= 2;
            }
            edge.slack -= target.dual;
        }
        // go through all vertices, greedily increase their dual variables to the minimum slack of incident edges
        // if there exist a tight unmatched edge in the neighborhood, match it
        int treeNum = nodeNum;
        Node node;
        for (int i = 0; i < nodeNum; i++) {
            node = nodes[i];
            if (!node.isInfinityNode()) {
                double minSlack = INFINITY;
                // find the minimum slack of incident edges
                for (Node.IncidentEdgeIterator incidentEdgeIterator = node.incidentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    if (edge.slack < minSlack) {
                        minSlack = edge.slack;
                    }
                }
                node.dual += minSlack;
                double resultMinSlack = minSlack;
                // subtract minimum slack from the slacks of all incident edges
                for (Node.IncidentEdgeIterator incidentEdgeIterator = node.incidentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    dir = incidentEdgeIterator.getDir();
                    if (edge.slack <= resultMinSlack && node.isPlusNode() && edge.head[dir].isPlusNode()) {
                        node.label = Node.Label.INFINITY;
                        edge.head[dir].label = Node.Label.INFINITY;
                        node.matched = edge;
                        edge.head[dir].matched = edge;
                        treeNum -= 2;
                    }
                    edge.slack -= resultMinSlack;
                }
            }
        }

        return treeNum;
    }

    /**
     * Initializes an auxiliary graph by adding tree edges between trees and adding (+, +) cross-tree edges
     * and (+, inf) edges to the appropriate heaps
     */
    private void initAuxiliaryGraph() {
        Node opposite;
        Tree tree;
        Edge edge;
        TreeEdge treeEdge;
        // go through all tree roots and visit all incident edges of those roots.
        // if a (+, inf) edge is encountered => add it to the infinity heap
        // if a (+, +) edge is encountered and the opposite node hasn't been processed yet =>
        // add this edge to the heap of (+, +) cross-tree edges
        for (Node root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            tree = root.tree;
            for (Node.IncidentEdgeIterator edgeIterator = root.incidentEdgesIterator(); edgeIterator.hasNext(); ) {
                edge = edgeIterator.next();
                opposite = edge.head[edgeIterator.getDir()];
                if (opposite.isInfinityNode()) {
                    tree.addPlusInfinityEdge(edge);
                } else if (!opposite.isProcessed) {
                    if (opposite.tree.currentEdge == null) {
                        State.addTreeEdge(tree, opposite.tree);
                    }
                    opposite.tree.currentEdge.addPlusPlusEdge(edge, edge.slack);
                }
            }
            root.isProcessed = true;
            for (Tree.TreeEdgeIterator treeEdgeIterator = tree.treeEdgeIterator(); treeEdgeIterator.hasNext(); ) {
                treeEdge = treeEdgeIterator.next();
                treeEdge.head[treeEdgeIterator.getCurrentDirection()].currentEdge = null;
            }
        }
        // clearing isProcessed flags
        for (Node root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.isProcessed = false;
        }
    }

    /**
     * Helper method for allocating trees. Initializes the doubly linked list of tree roots
     * via treeSiblingPrev and treeSiblingNext. The same mechanism is used for keeping track
     * of the children of a node in the tree. The node nodes[nodeNum] is used to quichly find
     * the first root in the linked list
     */
    private void allocateTrees() {
        Node lastRoot = nodes[nodeNum];
        for (int i = 0; i < nodeNum; i++) {
            Node node = nodes[i];
            if (node.isPlusNode()) {
                node.treeSiblingPrev = lastRoot;
                lastRoot.treeSiblingNext = node;
                lastRoot = node;
                new Tree(node);
            }
        }
        lastRoot.treeSiblingNext = null;
    }


}
