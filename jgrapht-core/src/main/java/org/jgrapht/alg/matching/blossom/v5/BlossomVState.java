/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.matching.blossom.v5;

import org.jgrapht.*;

import java.util.*;

/**
 * This class stores data needed for the Kolmogorov's Blossom V algorithm; it is used by
 * {@link KolmogorovWeightedPerfectMatching}, {@link BlossomVPrimalUpdater} and
 * {@link BlossomVDualUpdater} during the course of the algorithm.
 * <p>
 * We refer to this object with all the data stored in nodes, edges, trees, and tree edges as the
 * state of the algorithm
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see KolmogorovWeightedPerfectMatching
 * @see BlossomVPrimalUpdater
 * @see BlossomVDualUpdater
 */
class BlossomVState<V, E>
{
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
     * <p>
     * <b>Note:</b> the size of the array is nodeNum + 1. The node nodes[nodeNum] is an auxiliary
     * node that is used as the first element in the linked list of tree roots
     */
    BlossomVNode[] nodes;
    /**
     * An array of edges of the graph
     */
    BlossomVEdge[] edges;
    /**
     * Number of trees
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
    KolmogorovWeightedPerfectMatching.Statistics statistics;
    /**
     * BlossomVOptions used to determine the strategies used in the algorithm
     */
    BlossomVOptions options;
    /**
     * Initial generic vertices of the graph
     */
    List<V> graphVertices;
    /**
     * Initial edges of the graph
     */
    List<E> graphEdges;
    /**
     * Minimum edge weight in the graph
     */
    double minEdgeWeight;

    /**
     * Constructs the algorithm's initial state
     *
     * @param graph the graph for which to find a matching
     * @param nodes nodes used in the algorithm
     * @param edges edges used in the algorithm
     * @param nodeNum number of nodes in the graph
     * @param edgeNum number of edges in the graph
     * @param treeNum number of trees in the graph
     * @param graphVertices generic vertices of the {@code graph} in the same order as nodes in
     *        {@code nodes}
     * @param graphEdges generic edges of the {@code graph} in the same order as edges in
     *        {@code edges}
     * @param options default or user defined options
     */
    public BlossomVState(
        Graph<V, E> graph, BlossomVNode[] nodes, BlossomVEdge[] edges, int nodeNum, int edgeNum,
        int treeNum, List<V> graphVertices, List<E> graphEdges, BlossomVOptions options,
        double minEdgeWeight)
    {
        this.graph = graph;
        this.nodes = nodes;
        this.edges = edges;
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.treeNum = treeNum;
        this.graphVertices = graphVertices;
        this.graphEdges = graphEdges;
        this.options = options;
        this.statistics = new KolmogorovWeightedPerfectMatching.Statistics();
        this.minEdgeWeight = minEdgeWeight;
    }

}
