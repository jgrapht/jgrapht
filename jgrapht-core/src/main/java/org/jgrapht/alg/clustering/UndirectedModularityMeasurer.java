/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

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
public class UndirectedModularityMeasurer<V, E>
{

    private static final String INVALID_PARTITION_OF_VERTICES = "Invalid partition of vertices";
    private final Graph<V, E> graph;
    private double m;
    private Map<V, Double> degrees;

    /**
     * Construct a new measurer
     * 
     * @param graph the input graph
     */
    public UndirectedModularityMeasurer(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.degrees = new HashMap<>();
        precomputeDegrees(graph);
    }

    /**
     * Compute the modularity of a vertex partition.
     * 
     * @param partitions the partitions
     * @return the modularity
     */
    public double modularity(List<Set<V>> partitions)
    {
        // index partitions and count total (weighted) degree inside each partition
        int totalPartitions = partitions.size();
        Map<V, Integer> vertexPartition = new HashMap<>();
        double[] weightedDegreeInPartition = new double[totalPartitions];
        int curPartition = 0;
        for (Set<V> partition : partitions) {
            weightedDegreeInPartition[curPartition] = 0d;
            for (V v : partition) {
                vertexPartition.put(v, curPartition);
                Double d = degrees.get(v);
                if (d == null) {
                    throw new IllegalArgumentException(INVALID_PARTITION_OF_VERTICES);
                }
                weightedDegreeInPartition[curPartition] += d;
            }
            curPartition++;
        }

        // count (weighted) edges inside each partition
        double[] edgeWeightInPartition = new double[totalPartitions];
        for (E e : graph.edgeSet()) {
            V v = graph.getEdgeSource(e);
            V u = graph.getEdgeTarget(e);
            Integer pv = vertexPartition.get(v);
            if (pv == null) {
                throw new IllegalArgumentException(INVALID_PARTITION_OF_VERTICES);
            }
            Integer pu = vertexPartition.get(u);
            if (pu == null) {
                throw new IllegalArgumentException(INVALID_PARTITION_OF_VERTICES);
            }
            if (pv.intValue() == pu.intValue()) {
                // same partition
                edgeWeightInPartition[pv] += graph.getEdgeWeight(e);
            }
        }

        // compute modularity summing over partitions
        double mod = 0d;
        for (int p = 0; p < totalPartitions; p++) {
            double expectedEdgeWeightInPartition =
                weightedDegreeInPartition[p] * weightedDegreeInPartition[p] / (2d * m);
            mod += 2d * edgeWeightInPartition[p] - expectedEdgeWeightInPartition;
        }
        mod /= 2d * m;

        return mod;
    }

    /**
     * Pre-compute vertex (weighted) degrees.
     * 
     * @param graph the input graph
     */
    private void precomputeDegrees(Graph<V, E> graph)
    {
        if (graph.getType().isWeighted()) {
            m = graph.edgeSet().stream().collect(Collectors.summingDouble(graph::getEdgeWeight));
            for (V v : graph.vertexSet()) {
                double sum = 0d;
                for (E e : graph.outgoingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (u.equals(v)) {
                        sum += 2d * graph.getEdgeWeight(e);
                    } else {
                        sum += graph.getEdgeWeight(e);
                    }
                }
                degrees.put(v, sum);
            }
        } else {
            m = graph.edgeSet().size();
            for (V v : graph.vertexSet()) {
                // degreeof counts loops twice anyway
                degrees.put(v, Double.valueOf(graph.degreeOf(v)));
            }
        }
    }

}
