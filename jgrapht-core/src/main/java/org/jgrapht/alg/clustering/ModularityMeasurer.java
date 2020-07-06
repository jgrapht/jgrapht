/*
 * (C) Copyright 2020-2020, by Dimitrios Michail and Contributors.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;

/**
 * A <a href="https://en.wikipedia.org/wiki/Modularity_(networks)">modularity</a> measurer.
 * 
 * <p>
 * This is a utility class which computes the modularity function. It takes as input a list of
 * vertex classes $C$ and a graph $G$ and calculates: $Q = \frac{1}{2m} \sum_{ij} \left( A_{ij} -
 * \frac{k_i k_j}{2m} \right) \delta(C_i, C_j)$. Here $m$ is the total number of edges and $k_i$ is
 * the degree of vertex $i$. $A_{ij}$ is either $1$ or $0$ depending on whether edge $(i,j)$ belongs
 * to the graph and $\delta(C_i, C_j)$ is 1 if vertices $i$ and $j$ belong to the same class, $0$
 * otherwise.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class ModularityMeasurer<V, E>
{
    private static final String INVALID_PARTITION_OF_VERTICES = "Invalid partition of vertices";
    private final Graph<V, E> graph;
    private final boolean validatePartitions;
    private double m;
    private double normalization;
    private Map<V, Double> inDegrees;
    private Map<V, Double> outDegrees;

    /**
     * Construct a new measurer
     * 
     * @param graph the input graph
     */
    public ModularityMeasurer(Graph<V, E> graph)
    {
        this(graph, true);
    }

    /**
     * Construct a new measurer
     * 
     * @param graph the input graph
     * @param validatePartitions whether to first validate the input partitions. If false the user
     *        must make sure that she/he provides correct input.
     */
    public ModularityMeasurer(Graph<V, E> graph, boolean validatePartitions)
    {
        this.graph = Objects.requireNonNull(graph);
        this.validatePartitions = validatePartitions;
        this.inDegrees = new HashMap<>();
        this.outDegrees = new HashMap<>();

        precomputeDegrees(graph);
    }

    /**
     * Compute the modularity of a vertex partition.
     * 
     * @param partitions the partitions
     * @return the modularity
     */
    public double modulariry(List<Set<V>> partitions)
    {
        if (validatePartitions) {
            validatePartitions(partitions);
        }

        boolean isDirected = graph.getType().isDirected();
        double mod = 0d;
        for (Set<V> p : partitions) {
            for (V u : p) {
                for (V v : p) {
                    double w = 0d;
                    for (E e : graph.getAllEdges(u, v)) {
                        w += graph.getEdgeWeight(e);
                    }
                    if (u == v && !isDirected) {
                        w *= 2d;
                    }
                    mod += w - inDegrees.get(u) * outDegrees.get(v) * normalization;
                }
            }
        }

        return mod * normalization;
    }

    /**
     * Check that the input is indeed a partition of the vertices.
     * 
     * @param partitions the partitions to check
     */
    private void validatePartitions(List<Set<V>> partitions)
    {
        Set<V> used = new HashSet<>();
        for (Set<V> p : partitions) {
            for (V v : p) {
                if (!used.add(v) || !graph.containsVertex(v)) {
                    throw new IllegalArgumentException(INVALID_PARTITION_OF_VERTICES);
                }
            }
        }
        if (used.size() != graph.vertexSet().size()) {
            throw new IllegalArgumentException(INVALID_PARTITION_OF_VERTICES);
        }
    }

    /**
     * Pre-compute vertex (weighted) degrees and normalization factor.
     * 
     * @param graph the input graph
     */
    private void precomputeDegrees(Graph<V, E> graph)
    {
        GraphType type = graph.getType();

        if (type.isWeighted()) {
            m = graph.edgeSet().stream().collect(Collectors.summingDouble(graph::getEdgeWeight));
            for (V v : graph.vertexSet()) {
                double sum = 0d;
                for (E e : graph.incomingEdgesOf(v)) {
                    sum += graph.getEdgeWeight(e);
                }
                inDegrees.put(v, sum);
                sum = 0d;
                for (E e : graph.outgoingEdgesOf(v)) {
                    sum += graph.getEdgeWeight(e);
                }
                outDegrees.put(v, sum);
            }
        } else {
            m = graph.edgeSet().size();
            for (V v : graph.vertexSet()) {
                inDegrees.put(v, Double.valueOf(graph.inDegreeOf(v)));
                outDegrees.put(v, Double.valueOf(graph.outDegreeOf(v)));
            }
        }

        if (type.isDirected()) {
            normalization = 1.0 / m;
        } else {
            normalization = 1.0 / (2 * m);
        }
    }

}
