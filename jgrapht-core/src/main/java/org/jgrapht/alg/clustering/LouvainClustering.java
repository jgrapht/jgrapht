/*
 * (C) Copyright 2025-2025, by Rayene Abbassi and Contributors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

/**
 * Louvain community detection for undirected graphs.
 *
 * <p>
 * The implementation follows the multilevel modularity optimisation described in Blondel et al.
 * (2008). It supports weighted graphs, allows configuration of the modularity resolution parameter
 * and returns the best modularity partition discovered during optimisation.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class LouvainClustering<V, E>
    implements ClusteringAlgorithm<V>
{
    private static final double EPSILON = 1e-12;

    private final Graph<V, E> graph;
    private final double resolution;
    private final Random rng;
    private Clustering<V> result;

    /**
     * Create a Louvain clustering instance with resolution 1.0 and default randomness source.
     *
     * @param graph the (undirected) input graph
     */
    public LouvainClustering(Graph<V, E> graph)
    {
        this(graph, 1d);
    }

    /**
     * Create a Louvain clustering instance with custom resolution and default randomness source.
     *
     * @param graph the (undirected) input graph
     * @param resolution modularity resolution parameter (must be positive)
     */
    public LouvainClustering(Graph<V, E> graph, double resolution)
    {
        this(graph, resolution, new Random());
    }

    /**
     * Create a Louvain clustering instance with custom resolution and randomness source.
     *
     * @param graph the (undirected) input graph
     * @param resolution modularity resolution parameter (must be positive)
     * @param rng random number generator used to shuffle vertices during optimisation
     */
    public LouvainClustering(Graph<V, E> graph, double resolution, Random rng)
    {
        this.graph = GraphTests.requireUndirected(graph);
        if (!(resolution > 0d) || Double.isNaN(resolution) || Double.isInfinite(resolution)) {
            throw new IllegalArgumentException("Resolution must be a positive finite number");
        }
        this.resolution = resolution;
        this.rng = Objects.requireNonNull(rng);
    }

    @Override
    public Clustering<V> getClustering()
    {
        if (result == null) {
            List<Set<V>> clusters = new Implementation<>(graph, resolution, rng).compute();
            result = new ClusteringImpl<>(clusters);
        }
        return result;
    }

    private static final class Implementation<V, E>
    {
        private final Graph<V, E> graph;
        private final double resolution;
        private final Random rng;

        Implementation(Graph<V, E> graph, double resolution, Random rng)
        {
            this.graph = graph;
            this.resolution = resolution;
            this.rng = rng;
        }

        List<Set<V>> compute()
        {
            List<V> vertices = new ArrayList<>(graph.vertexSet());
            if (vertices.isEmpty()) {
                return Collections.emptyList();
            }

            Map<V, Integer> index = new HashMap<>();
            for (int i = 0; i < vertices.size(); i++) {
                index.put(vertices.get(i), i);
            }

            Level level = Level.fromGraph(graph, vertices, index);
            double bestModularity = Double.NEGATIVE_INFINITY;
            List<int[]> assignments = new ArrayList<>();

            while (true) {
                LocalMovingResult result = level.runLocalMoving(resolution, rng);
                double modularity = level.computeModularity(result.assignment, resolution);
                if (modularity <= bestModularity + EPSILON) {
                    break;
                }

                assignments.add(result.assignment);
                bestModularity = modularity;

                if (!result.improved || result.communityCount == level.size()) {
                    break;
                }

                level = level.aggregate(result.assignment, result.communityCount);
            }

            return buildClusters(vertices, assignments);
        }

        private List<Set<V>> buildClusters(List<V> vertices, List<int[]> assignments)
        {
            if (assignments.isEmpty()) {
                List<Set<V>> clusters = new ArrayList<>();
                for (V vertex : vertices) {
                    Set<V> singleton = new LinkedHashSet<>();
                    singleton.add(vertex);
                    clusters.add(singleton);
                }
                return clusters;
            }

            int n = vertices.size();
            int[] finalAssignment = new int[n];
            for (int v = 0; v < n; v++) {
                int community = assignments.get(0)[v];
                for (int level = 1; level < assignments.size(); level++) {
                    community = assignments.get(level)[community];
                }
                finalAssignment[v] = community;
            }

            Map<Integer, Set<V>> clusters = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                int community = finalAssignment[i];
                clusters.computeIfAbsent(community, k -> new LinkedHashSet<>()).add(vertices.get(i));
            }

            return new ArrayList<>(clusters.values());
        }
    }

    private static final class Level
    {
        private final List<Map<Integer, Double>> adjacency;
        private final double[] nodeDegree;
        private final double m2;

        private Level(List<Map<Integer, Double>> adjacency, double[] nodeDegree, double m2)
        {
            this.adjacency = adjacency;
            this.nodeDegree = nodeDegree;
            this.m2 = m2;
        }

        static <V, E> Level fromGraph(
            Graph<V, E> graph, List<V> vertices, Map<V, Integer> index)
        {
            int n = vertices.size();
            List<Map<Integer, Double>> adjacency = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                adjacency.add(new HashMap<>());
            }

            double[] degree = new double[n];
            double totalDegree = 0d;
            boolean weighted = graph.getType().isWeighted();

            for (E edge : graph.edgeSet()) {
                V source = graph.getEdgeSource(edge);
                V target = graph.getEdgeTarget(edge);
                Integer u = index.get(source);
                Integer v = index.get(target);
                if (u == null || v == null) {
                    throw new IllegalArgumentException("Edge contains vertex not in vertex set");
                }

                double weight = weighted ? graph.getEdgeWeight(edge) : 1d;
                if (Double.isNaN(weight) || weight < 0d) {
                    throw new IllegalArgumentException("Graph contains negative or NaN edge weight");
                }

                if (u.intValue() == v.intValue()) {
                    adjacency.get(u).merge(v, weight, Double::sum);
                    degree[u] += 2d * weight;
                    totalDegree += 2d * weight;
                } else {
                    adjacency.get(u).merge(v, weight, Double::sum);
                    adjacency.get(v).merge(u, weight, Double::sum);
                    degree[u] += weight;
                    degree[v] += weight;
                    totalDegree += 2d * weight;
                }
            }

            return new Level(adjacency, degree, totalDegree);
        }

        int size()
        {
            return nodeDegree.length;
        }

        LocalMovingResult runLocalMoving(double resolution, Random rng)
        {
            int n = nodeDegree.length;
            if (n == 0) {
                return new LocalMovingResult(new int[0], 0, false);
            }

            int[] community = new int[n];
            double[] communityWeight = new double[n];
            for (int i = 0; i < n; i++) {
                community[i] = i;
                communityWeight[i] = nodeDegree[i];
            }

            if (m2 < EPSILON) {
                int[] assignment = new int[n];
                for (int i = 0; i < n; i++) {
                    assignment[i] = i;
                }
                return new LocalMovingResult(assignment, n, false);
            }

            int[] order = new int[n];
            for (int i = 0; i < n; i++) {
                order[i] = i;
            }

            boolean updated = false;
            Map<Integer, Double> weightsBuffer = new HashMap<>();

            boolean moved;
            do {
                moved = false;
                shuffle(order, rng);
                for (int idx = 0; idx < n; idx++) {
                    int node = order[idx];
                    double nodeWeight = nodeDegree[node];
                    if (nodeWeight < EPSILON) {
                        continue;
                    }

                    int currentCommunity = community[node];
                    communityWeight[currentCommunity] -= nodeWeight;

                    weightsBuffer.clear();
                    for (Entry<Integer, Double> entry : adjacency.get(node).entrySet()) {
                        int neighbor = entry.getKey();
                        double weight = entry.getValue();
                        int neighborCommunity = community[neighbor];
                        weightsBuffer.merge(neighborCommunity, weight, Double::sum);
                    }
                    weightsBuffer.putIfAbsent(currentCommunity, 0d);

                    int bestCommunity = currentCommunity;
                    double bestGain = 0d;
                    double bestWeight = weightsBuffer.get(currentCommunity);

                    for (Entry<Integer, Double> entry : weightsBuffer.entrySet()) {
                        int candidate = entry.getKey();
                        double edgeWeight = entry.getValue();
                        double candidateTot = communityWeight[candidate];
                        double gain = edgeWeight - resolution * candidateTot * nodeWeight / m2;

                        if (gain > bestGain + EPSILON
                            || (Math.abs(gain - bestGain) <= EPSILON
                                && edgeWeight > bestWeight + EPSILON)) {
                            bestGain = gain;
                            bestWeight = edgeWeight;
                            bestCommunity = candidate;
                        }
                    }

                    communityWeight[bestCommunity] += nodeWeight;
                    if (bestCommunity != currentCommunity) {
                        community[node] = bestCommunity;
                        moved = true;
                        updated = true;
                    }
                }
            } while (moved);

            return compressCommunities(community, updated);
        }

        double computeModularity(int[] assignment, double resolution)
        {
            if (assignment.length == 0 || m2 < EPSILON) {
                return 0d;
            }

            int communities = Arrays.stream(assignment).max().orElse(-1) + 1;
            double[] communityDegree = new double[communities];
            double[] communityInternal = new double[communities];

            for (int i = 0; i < assignment.length; i++) {
                int community = assignment[i];
                communityDegree[community] += nodeDegree[i];
                for (Entry<Integer, Double> edge : adjacency.get(i).entrySet()) {
                    int j = edge.getKey();
                    if (i <= j && assignment[j] == community) {
                        communityInternal[community] += edge.getValue();
                    }
                }
            }

            double modularity = 0d;
            for (int c = 0; c < communities; c++) {
                double in = communityInternal[c];
                double tot = communityDegree[c];
                modularity += (in / m2) - resolution * (tot / m2) * (tot / m2);
            }

            return modularity;
        }

        Level aggregate(int[] assignment, int communityCount)
        {
            List<Map<Integer, Double>> newAdjacency = new ArrayList<>(communityCount);
            for (int i = 0; i < communityCount; i++) {
                newAdjacency.add(new HashMap<>());
            }
            double[] newDegree = new double[communityCount];
            double[] newSelfLoops = new double[communityCount];

            for (int i = 0; i < assignment.length; i++) {
                int community = assignment[i];
                newDegree[community] += nodeDegree[i];
            }

            for (int i = 0; i < assignment.length; i++) {
                int ci = assignment[i];
                for (Entry<Integer, Double> edge : adjacency.get(i).entrySet()) {
                    int j = edge.getKey();
                    if (i > j) {
                        continue;
                    }
                    double weight = edge.getValue();
                    int cj = assignment[j];
                    if (ci == cj) {
                        newSelfLoops[ci] += weight;
                    } else {
                        newAdjacency.get(ci).merge(cj, weight, Double::sum);
                        newAdjacency.get(cj).merge(ci, weight, Double::sum);
                    }
                }
            }

            for (int c = 0; c < communityCount; c++) {
                if (newSelfLoops[c] > 0d) {
                    newAdjacency.get(c).put(c, newSelfLoops[c]);
                }
            }

            return new Level(newAdjacency, newDegree, m2);
        }

        private LocalMovingResult compressCommunities(int[] community, boolean updated)
        {
            Map<Integer, Integer> mapping = new LinkedHashMap<>();
            int[] assignment = new int[community.length];
            int next = 0;
            for (int i = 0; i < community.length; i++) {
                int cid = community[i];
                Integer mapped = mapping.get(cid);
                if (mapped == null) {
                    mapped = next++;
                    mapping.put(cid, mapped);
                }
                assignment[i] = mapped;
            }
            return new LocalMovingResult(assignment, next, updated);
        }

        private static void shuffle(int[] order, Random rng)
        {
            for (int i = order.length - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int tmp = order[i];
                order[i] = order[j];
                order[j] = tmp;
            }
        }
    }

    private static final class LocalMovingResult
    {
        final int[] assignment;
        final int communityCount;
        final boolean improved;

        LocalMovingResult(int[] assignment, int communityCount, boolean improved)
        {
            this.assignment = assignment;
            this.communityCount = communityCount;
            this.improved = improved;
        }
    }
}
