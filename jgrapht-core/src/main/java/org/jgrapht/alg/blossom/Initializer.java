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
package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

import static org.jgrapht.alg.blossom.KolmogorovMinimumWeightPerfectMatching.INFINITY;

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
    private int nodeNum;
    /**
     * Number of edges in the graph
     */
    private int edgeNum;
    /**
     * An array of nodes that will be passes to the resulting state object
     */
    private Node[] nodes;
    /**
     * An array of edges that will be passes to the resulting state object
     */
    private Edge[] edges;
    /**
     * A mapping from initial graph's vertices to nodes that will be passes to the resulting state object
     */
    private Map<V, Node> vertexMap;
    /**
     * A mapping from the initial graph's edges to the internal edge representations that will be passes
     * to the resulting state object
     */
    private Map<E, Edge> edgeMap;

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
    public State<V, E> initialize(KolmogorovMinimumWeightPerfectMatching.Options options) {
        InitializationType type = options.initializationType;
        initGraph();
        State<V, E> state = new State<>(graph, nodes, edges, nodeNum, edgeNum, 0, vertexMap, edgeMap, options);

        int treeNum;
        if (type == InitializationType.GREEDY) {
            treeNum = initGreedy();
        } else {
            treeNum = nodeNum;
            for (Node node : nodes) {
                node.isOuter = true;
            }
        }
        allocateTrees();
        state.treeNum = treeNum;
        initAuxiliaryGraph();
        for (Node root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            root.isProcessed = false;
        }

        return state;
    }

    private void initGraph() {
        nodeNum = graph.vertexSet().size();
        nodes = new Node[nodeNum + 1];
        nodes[nodeNum] = new Node();  // helper node to keep track of the first item in the linked list of tree roots
        edges = new Edge[graph.edgeSet().size()];
        vertexMap = new HashMap<>(nodeNum);
        edgeMap = new HashMap<>(edgeNum);
        int i = 0;
        for (V vertex : graph.vertexSet()) {
            nodes[i] = new Node();
            vertexMap.put(vertex, nodes[i]);
            i++;
        }
        i = 0;
        for (E e : graph.edgeSet()) {
            Node source = vertexMap.get(graph.getEdgeSource(e));
            Node target = vertexMap.get(graph.getEdgeTarget(e));
            if (source != target) {
                edgeNum++;
                Edge edge = State.addEdge(source, target, graph.getEdgeWeight(e));
                edges[i] = edge;
                edgeMap.put(e, edge);
                i++;
            }
        }
    }

    private int initGreedy() {
        int dir;
        Edge edge;
        // set all dual variables to infinity
        for (int i = 0; i < nodeNum; i++) {
            nodes[i].dual = INFINITY;
        }
        // set dual variables to the minimum weight of the incident edges
        for (int i = 0; i < edgeNum; i++) {
            edge = edges[i];
            if (edge.head[0].dual > edge.slack) {
                edge.head[0].dual = edge.slack;
            }
            if (edge.head[1].dual > edge.slack) {
                edge.head[1].dual = edge.slack;
            }
        }
        // divide dual variables by to, decrease edge slack accordingly
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
        // if there exist an tight unmatched edge in the neighborhood, match it
        int treeNum = nodeNum;
        for (Node node : nodes) {
            if (!node.isInfinityNode()) {
                double minSlack = INFINITY;
                for (Node.IncidentEdgeIterator incidentEdgeIterator = node.adjacentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    if (edge.slack < minSlack) {
                        minSlack = edge.slack;
                    }
                }
                node.dual += minSlack;
                double resultMinSlack = minSlack;
                for (Node.IncidentEdgeIterator incidentEdgeIterator = node.adjacentEdgesIterator(); incidentEdgeIterator.hasNext(); ) {
                    edge = incidentEdgeIterator.next();
                    dir = incidentEdgeIterator.getDir();
                    if (edge.slack <= resultMinSlack && node.isPlusNode() && edge.head[dir].isPlusNode()) {
                        node.setLabel(Node.Label.INFINITY);
                        edge.head[dir].setLabel(Node.Label.INFINITY);
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

    private void initAuxiliaryGraph() {
        Node opposite;
        Tree tree;
        Edge edge;
        TreeEdge treeEdge;
        for (Node root = nodes[nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            tree = root.tree;
            for (Node.IncidentEdgeIterator edgeIterator = root.adjacentEdgesIterator(); edgeIterator.hasNext(); ) {
                edge = edgeIterator.next();
                opposite = edge.head[edgeIterator.getDir()];
                if (opposite.isInfinityNode()) {
                    tree.addPlusInfinityEdge(edge, edge.slack);
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
    }

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

    enum InitializationType {
        GREEDY, NONE,
    }
}
