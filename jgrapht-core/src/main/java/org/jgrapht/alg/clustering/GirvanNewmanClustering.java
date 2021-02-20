/*
 * (C) Copyright 2021-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.clustering;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality;
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality.OverflowStrategy;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

/**
 * The Girvan-Newman clustering algorithm.
 * 
 * <p>
 * The algorithm is described in: Girvan, Michelle, and Mark EJ Newman. "Community structure in
 * social and biological networks." Proceedings of the national academy of sciences 99.12 (2002):
 * 7821-7826.
 * 
 * <p>
 * Running time is $O(m^2 n)$ or $O(m^2n + m n^2 \log n)$ for weighted graphs.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class GirvanNewmanClustering<V, E>
    implements
    ClusteringAlgorithm<V>
{
    private Graph<V, E> graph;
    private int k;
    private final Iterable<V> startVertices;
    private final OverflowStrategy overflowStrategy;

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph
     * @param k the desired number of clusters
     */
    public GirvanNewmanClustering(Graph<V, E> graph, int k)
    {
        this(graph, k, OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW, graph.vertexSet());
    }

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph
     * @param k the desired number of clusters
     * @param overflowStrategy strategy to use if overflow is detected
     * @param startVertices vertices from which to start shortest path computations when computing
     *        edge centralities. This parameter allows the user to compute edge centrality
     *        contributions only from a subset of the vertices of the graph. If null the whole graph
     *        vertex set is used.
     */
    public GirvanNewmanClustering(
        Graph<V, E> graph, int k, OverflowStrategy overflowStrategy, Iterable<V> startVertices)
    {
        this.graph = Objects.requireNonNull(graph);
        if (k < 1 || k > graph.vertexSet().size()) {
            throw new IllegalArgumentException("Illegal number of clusters");
        }
        this.k = k;
        this.overflowStrategy = overflowStrategy;
        if (startVertices == null) {
            this.startVertices = graph.vertexSet();
        } else {
            this.startVertices = startVertices;
        }
    }

    @Override
    public Clustering<V> getClustering()
    {
        // copy graph
        Graph<V,
            DefaultEdge> graphCopy = GraphTypeBuilder
                .forGraphType(graph.getType()).edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(graph.getVertexSupplier()).buildGraph();
        for (V v : graph.iterables().vertices()) {
            graphCopy.addVertex(v);
        }
        for (E e : graph.iterables().edges()) {
            V sourceVertex = graph.getEdgeSource(e);
            V targetVertex = graph.getEdgeTarget(e);
            graphCopy.addEdge(sourceVertex, targetVertex);
        }

        // main algorithm
        while (true) {
            List<Set<V>> ccs = new ConnectivityInspector<>(graphCopy).connectedSets();
            if (ccs.size() == k) {
                return new ClusteringImpl<>(ccs);
            }

            // compute edge centralities
            EdgeBetweennessCentrality<V, DefaultEdge> bc =
                new EdgeBetweennessCentrality<>(graphCopy, overflowStrategy, startVertices);

            // find edge with max centrality
            DefaultEdge maxEdge = null;
            double maxCentrality = 0d;
            for (Entry<DefaultEdge, Double> entry : bc.getScores().entrySet()) {
                if (Double.compare(entry.getValue(), maxCentrality) > 0 || maxEdge == null) {
                    maxEdge = entry.getKey();
                    maxCentrality = entry.getValue();
                }
            }

            // remove edge with max centrality
            graphCopy.removeEdge(maxEdge);
        }
    }

}
