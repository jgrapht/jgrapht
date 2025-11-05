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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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
 * <h2>Leiden community detection (Modularity &amp; CPM)</h2>
 *
 * <p>
 * Implementation of the Leiden algorithm for undirected graphs, supporting both
 * <em>Modularity</em> and <em>CPM (Constant Potts Model)</em> quality functions.
 * The algorithm follows the three-phase Leiden scheme:
 * </p>
 *
 * <ol>
 *   <li><b>Local movement</b> of nodes to improve a quality function (modularity or CPM)</li>
 *   <li><b>Refinement</b> that splits communities into internally connected subcommunities</li>
 *   <li><b>Aggregation</b> of communities into a super-graph, then repeat</li>
 * </ol>
 *
 * <p>
 * References:
 * <ul>
 *   <li>V. A. Traag, L. Waltman, N. J. van Eck (2019). From Louvain to Leiden:
 *       guaranteeing well-connected communities. <em>Scientific Reports</em>.</li>
 *   <li>V. A. Traag et al. on CPM (Constant Potts Model) quality function.</li>
 * </ul>
 * </p>
 *
 * <h3>Quality functions</h3>
 * <ul>
 *   <li><b>Modularity</b> (with resolution γ): compares against a null model and is the
 *       traditional objective used in Louvain. Suffers from the resolution limit.</li>
 *   <li><b>CPM</b> (with resolution γ): density-based Potts objective, recommended by the
 *       Leiden paper to avoid the resolution limit.</li>
 * </ul>
 *
 * <p>
 * This class mirrors the public API style of {@link LouvainClustering}: call {@link #getClustering()}
 * to obtain a {@code Clustering<V>} result. Weighted graphs are supported. Self-loops are supported.
 * </p>
 *
 * <p><b>CPM definition used here (weighted, undirected):</b>
 * Let C be the partition, n_c the size of community c, and
 * {@code in_c} the total internal edge weight of c (each edge counted once).
 * We maximize
 * <pre>
 *   Q_CPM(C) = sum_c [ in_c - γ * n_c * (n_c - 1) / 2 ]
 * </pre>
 * When moving a node i into community c during local movement (evaluated after temporarily
 * removing i from its current community), the approximate gain is:
 * <pre>
 *   ΔQ_CPM = w(i → c) - γ * |c|
 * </pre>
 * where {@code w(i → c)} is the sum of weights from i to nodes in c, and |c| is the
 * current size of community c (after removal of i from its original community).
 * </p>
 *
 * <p><b>Modularity definition (with resolution γ):</b>
 * Using total weight {@code m2 = 2m} (twice the sum of all edge weights) and degree k_i,
 * the marginal gain when moving node i to community c is:
 * <pre>
 *   ΔQ_mod = w(i → c) - γ * (tot_c * k_i / m2)
 * </pre>
 * where tot_c is the sum of degrees (weighted) of nodes in c.
 * </p>
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LeidenClustering<V, E>
        implements ClusteringAlgorithm<V>
{
    private static final double EPSILON = 1e-12;

    /** Selects which quality function to optimize. */
    public enum Quality {
        /** Modularity with resolution γ (traditional; suffers from resolution limit). */
        MODULARITY,
        /** Constant Potts Model with resolution γ (recommended in the Leiden paper). */
        CPM
    }

    private final Graph<V, E> graph;
    private final double resolution;
    private final Random rng;
    private final Quality quality;
    private Clustering<V> result;

    /**
     * Construct Leiden with Modularity (γ=1.0) and default RNG.
     *
     * @param graph undirected input graph
     */
    public LeidenClustering(Graph<V, E> graph)
    {
        this(graph, 1d, new Random(), Quality.MODULARITY);
    }

    /**
     * Construct Leiden with given γ and Modularity, default RNG.
     *
     * @param graph undirected input graph
     * @param resolution quality function resolution γ (must be positive and finite)
     */
    public LeidenClustering(Graph<V, E> graph, double resolution)
    {
        this(graph, resolution, new Random(), Quality.MODULARITY);
    }

    /**
     * Construct Leiden with given γ, RNG, and quality function.
     *
     * @param graph undirected input graph
     * @param resolution quality function resolution γ (must be positive and finite)
     * @param rng randomness source for vertex order shuffling
     * @param quality which quality function to optimize (MODULARITY or CPM)
     */
    public LeidenClustering(Graph<V, E> graph, double resolution, Random rng, Quality quality)
    {
        this.graph = GraphTests.requireUndirected(graph);
        if (!(resolution > 0d) || Double.isNaN(resolution) || Double.isInfinite(resolution)) {
            throw new IllegalArgumentException("Resolution must be a positive finite number");
        }
        this.resolution = resolution;
        this.rng = Objects.requireNonNull(rng);
        this.quality = Objects.requireNonNull(quality);
    }

    @Override
    public Clustering<V> getClustering()
    {
        if (result == null) {
            List<Set<V>> clusters = new Implementation<>(graph, resolution, rng, quality).compute();
            result = new ClusteringImpl<>(clusters);
        }
        return result;
    }

    // =====================================================================

    private static final class Implementation<V, E>
    {
        private final Graph<V, E> graph;
        private final double resolution;
        private final Random rng;
        private final Quality quality;

        Implementation(Graph<V, E> graph, double resolution, Random rng, Quality quality)
        {
            this.graph = graph;
            this.resolution = resolution;
            this.rng = rng;
            this.quality = quality;
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

            double bestScore = Double.NEGATIVE_INFINITY;
            List<int[]> assignments = new ArrayList<>();

            while (true) {
                // Phase 1: local moving from singletons
                LocalMovingResult lm = level.runLocalMoving(resolution, rng, quality);
                int[] afterLM = lm.assignment;

                // Phase 2: refinement (guarantees internal connectivity)
                LocalMovingResult refined = level.refinePartition(afterLM);
                int[] afterRefine = refined.assignment;

                // Extra local moving pass starting from refined partition
                LocalMovingResult postRefineLM = level.runLocalMovingFrom(resolution, rng, quality, afterRefine);
                int[] afterPostLM = postRefineLM.assignment;

                // Evaluate
                double score = level.computeQuality(afterPostLM, resolution, quality);
                if (score <= bestScore + EPSILON) {
                    break;
                }

                assignments.add(afterPostLM);
                bestScore = score;

                int communityCount = postRefineLM.communityCount;
                boolean anyImproved = lm.improved || refined.improved || postRefineLM.improved;
                if (!anyImproved || communityCount == level.size()) {
                    break;
                }

                // Phase 3: aggregation and repeat
                level = level.aggregate(afterPostLM, communityCount);
            }

            return buildClusters(vertices, assignments);
        }

        private List<Set<V>> buildClusters(List<V> vertices, List<int[]> assignments)
        {
            if (assignments.isEmpty()) {
                List<Set<V>> clusters = new ArrayList<>();
                for (V v : vertices) {
                    Set<V> s = new LinkedHashSet<>();
                    s.add(v);
                    clusters.add(s);
                }
                return clusters;
            }

            int n = vertices.size();
            int[] finalAssignment = new int[n];
            for (int v = 0; v < n; v++) {
                int c = assignments.get(0)[v];
                for (int lvl = 1; lvl < assignments.size(); lvl++) {
                    c = assignments.get(lvl)[c];
                }
                finalAssignment[v] = c;
            }

            Map<Integer, Set<V>> map = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                int c = finalAssignment[i];
                map.computeIfAbsent(c, k -> new LinkedHashSet<>()).add(vertices.get(i));
            }
            return new ArrayList<>(map.values());
        }
    }

    // =====================================================================

    /**
     * Internal level representation (weighted adjacency + degree + total weight).
     * Provides Leiden phases and both quality functions.
     */
    private static final class Level
    {
        private final List<Map<Integer, Double>> adjacency;
        private final double[] nodeDegree; // weighted degree k_i
        private final double m2;           // 2 * total edge weight

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
                V s = graph.getEdgeSource(edge);
                V t = graph.getEdgeTarget(edge);
                Integer u = index.get(s);
                Integer v = index.get(t);
                if (u == null || v == null) {
                    throw new IllegalArgumentException("Edge contains vertex not in vertex set");
                }

                double w = weighted ? graph.getEdgeWeight(edge) : 1d;
                if (Double.isNaN(w) || w < 0d) {
                    throw new IllegalArgumentException("Graph contains negative or NaN edge weight");
                }

                if (u.intValue() == v.intValue()) {
                    adjacency.get(u).merge(v, w, Double::sum);
                    degree[u] += 2d * w;
                    totalDegree += 2d * w;
                } else {
                    adjacency.get(u).merge(v, w, Double::sum);
                    adjacency.get(v).merge(u, w, Double::sum);
                    degree[u] += w;
                    degree[v] += w;
                    totalDegree += 2d * w;
                }
            }

            return new Level(adjacency, degree, totalDegree);
        }

        int size() { return nodeDegree.length; }

        // ------------------------ Leiden Phase 1: local moving (from singletons) ------------------------

        LocalMovingResult runLocalMoving(double resolution, Random rng, Quality quality)
        {
            int n = nodeDegree.length;
            if (n == 0) {
                return new LocalMovingResult(new int[0], 0, false);
            }

            int[] community = new int[n];
            double[] communityWeight = new double[n]; // sum of degrees in community (for modularity)
            int[] communitySize = new int[n];         // |C| (for CPM)
            for (int i = 0; i < n; i++) {
                community[i] = i;
                communityWeight[i] = nodeDegree[i];
                communitySize[i] = 1;
            }

            if (isEdgeless()) {
                int[] assignment = new int[n];
                for (int i = 0; i < n; i++) assignment[i] = i;
                return new LocalMovingResult(assignment, n, false);
            }

            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;

            boolean updated = false;
            Map<Integer, Double> weightsBuffer = new HashMap<>();

            boolean moved;
            do {
                moved = false;
                shuffle(order, rng);
                for (int idx = 0; idx < n; idx++) {
                    int node = order[idx];
                    double k_i = nodeDegree[node];
                    int current = community[node];

                    // Remove node from its current community (temporary)
                    communityWeight[current] -= k_i;
                    communitySize[current] -= 1;

                    // Accumulate weights from node to neighboring communities
                    weightsBuffer.clear();
                    for (Entry<Integer, Double> e : adjacency.get(node).entrySet()) {
                        int nb = e.getKey();
                        double w = e.getValue();
                        int nbCom = community[nb];
                        weightsBuffer.merge(nbCom, w, Double::sum);
                    }
                    // Consider staying (gain baseline 0 with current community stats updated)
                    weightsBuffer.putIfAbsent(current, 0d);

                    int bestCom = current;
                    double bestGain = 0d;
                    double bestTiebreak = weightsBuffer.get(current); // prefer stronger attachment

                    for (Entry<Integer, Double> e : weightsBuffer.entrySet()) {
                        int cand = e.getKey();
                        double wToCand = e.getValue();

                        double gain;
                        if (quality == Quality.MODULARITY) {
                            double totCand = communityWeight[cand];
                            gain = wToCand - resolution * totCand * k_i / m2;
                        } else { // CPM
                            int sizeCand = communitySize[cand];
                            gain = wToCand - resolution * sizeCand;
                        }

                        if (gain > bestGain + EPSILON ||
                           (Math.abs(gain - bestGain) <= EPSILON && wToCand > bestTiebreak + EPSILON)) {
                            bestGain = gain;
                            bestTiebreak = wToCand;
                            bestCom = cand;
                        }
                    }

                    // Place node back (in best community)
                    community[node] = bestCom;
                    communityWeight[bestCom] += k_i;
                    communitySize[bestCom] += 1;

                    if (bestCom != current) {
                        moved = true;
                        updated = true;
                    }
                }
            } while (moved);

            return compressCommunities(community, updated);
        }

        // ---------------- Local moving starting from an existing assignment ----------------

        LocalMovingResult runLocalMovingFrom(double resolution, Random rng, Quality quality, int[] initial)
        {
            int n = nodeDegree.length;
            if (n == 0) {
                return new LocalMovingResult(new int[0], 0, false);
            }
            if (initial.length != n) {
                throw new IllegalArgumentException("Initial assignment length mismatch");
            }

            LocalMovingResult comp = compressCommunities(initial, false);
            int[] community = Arrays.copyOf(comp.assignment, n);
            int communities = comp.communityCount;

            double[] communityWeight = new double[communities];
            int[] communitySize = new int[communities];
            for (int i = 0; i < n; i++) {
                int c = community[i];
                communityWeight[c] += nodeDegree[i];
                communitySize[c] += 1;
            }

            if (isEdgeless()) {
                return new LocalMovingResult(community, communities, false);
            }

            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;

            boolean updated = false;
            Map<Integer, Double> weightsBuffer = new HashMap<>();

            boolean moved;
            do {
                moved = false;
                shuffle(order, rng);
                for (int idx = 0; idx < n; idx++) {
                    int node = order[idx];
                    double k_i = nodeDegree[node];
                    int current = community[node];

                    communityWeight[current] -= k_i;
                    communitySize[current] -= 1;

                    weightsBuffer.clear();
                    for (Entry<Integer, Double> e : adjacency.get(node).entrySet()) {
                        int nb = e.getKey();
                        double w = e.getValue();
                        int nbCom = community[nb];
                        weightsBuffer.merge(nbCom, w, Double::sum);
                    }
                    weightsBuffer.putIfAbsent(current, 0d);

                    int bestCom = current;
                    double bestGain = 0d;
                    double bestTiebreak = weightsBuffer.get(current);

                    for (Entry<Integer, Double> e : weightsBuffer.entrySet()) {
                        int cand = e.getKey();
                        double wToCand = e.getValue();

                        double gain;
                        if (quality == Quality.MODULARITY) {
                            double totCand = communityWeight[cand];
                            gain = wToCand - resolution * totCand * k_i / m2;
                        } else {
                            int sizeCand = communitySize[cand];
                            gain = wToCand - resolution * sizeCand;
                        }

                        if (gain > bestGain + EPSILON ||
                           (Math.abs(gain - bestGain) <= EPSILON && wToCand > bestTiebreak + EPSILON)) {
                            bestGain = gain;
                            bestTiebreak = wToCand;
                            bestCom = cand;
                        }
                    }

                    community[ node ] = bestCom;
                    communityWeight[bestCom] += k_i;
                    communitySize[bestCom] += 1;

                    if (bestCom != current) {
                        moved = true;
                        updated = true;
                    }
                }
            } while (moved);

            return compressCommunities(community, updated);
        }

        // ------------------------ Leiden Phase 2: refinement ------------------------

        /**
         * Split any community that is not internally connected into its connected components.
         * This ensures Leiden's well-connectedness guarantee.
         */
        LocalMovingResult refinePartition(int[] assignment)
        {
            int n = nodeDegree.length;
            if (assignment.length != n) {
                throw new IllegalArgumentException("Assignment length mismatch");
            }
            if (n == 0 || isEdgeless()) {
                return compressCommunities(assignment, false);
            }

            Map<Integer, List<Integer>> byCommunity = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                byCommunity.computeIfAbsent(assignment[i], k -> new ArrayList<>()).add(i);
            }

            int[] refined = Arrays.copyOf(assignment, n);
            int nextId = Arrays.stream(assignment).max().orElse(-1) + 1;
            boolean changed = false;

            for (Entry<Integer, List<Integer>> e : byCommunity.entrySet()) {
                List<Integer> members = e.getValue();
                if (members.size() <= 1) continue;

                Map<Integer, Boolean> inC = new HashMap<>(members.size() * 2);
                for (int v : members) inC.put(v, Boolean.TRUE);

                Map<Integer, Integer> compId = new HashMap<>();
                int compCount = 0;

                for (int v : members) {
                    if (compId.containsKey(v)) continue;
                    int id = compCount++;
                    Deque<Integer> dq = new ArrayDeque<>();
                    dq.add(v);
                    compId.put(v, id);
                    while (!dq.isEmpty()) {
                        int u = dq.removeFirst();
                        for (Entry<Integer, Double> nb : adjacency.get(u).entrySet()) {
                            int w = nb.getKey();
                            if (!inC.containsKey(w)) continue;
                            if (!compId.containsKey(w)) {
                                compId.put(w, id);
                                dq.addLast(w);
                            }
                        }
                    }
                }

                if (compCount <= 1) continue;

                changed = true;
                int base = e.getKey();
                Map<Integer, Integer> map = new HashMap<>();
                map.put(0, base);
                for (int c = 1; c < compCount; c++) {
                    map.put(c, nextId++);
                }
                for (Entry<Integer, Integer> ci : compId.entrySet()) {
                    int node = ci.getKey();
                    refined[node] = map.get(ci.getValue());
                }
            }

            return compressCommunities(refined, changed);
        }

        // ------------------------ Quality evaluation ------------------------

        double computeQuality(int[] assignment, double resolution, Quality quality)
        {
            if (assignment.length == 0) return 0d;

            if (quality == Quality.MODULARITY) {
                if (isEdgeless()) return 0d;
                int communities = Arrays.stream(assignment).max().orElse(-1) + 1;
                double[] communityDegree = new double[communities];
                double[] communityInternal = new double[communities];

                for (int i = 0; i < assignment.length; i++) {
                    int c = assignment[i];
                    communityDegree[c] += nodeDegree[i];
                    for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                        int j = e.getKey();
                        if (i <= j && assignment[j] == c) {
                            communityInternal[c] += e.getValue();
                        }
                    }
                }

                double q = 0d;
                for (int c = 0; c < communities; c++) {
                    double in = communityInternal[c];
                    double tot = communityDegree[c];
                    q += (in / m2) - resolution * (tot / m2) * (tot / m2);
                }
                return q;
            } else { // CPM
                int communities = Arrays.stream(assignment).max().orElse(-1) + 1;
                int[] size = new int[communities];
                double[] internal = new double[communities];

                for (int i = 0; i < assignment.length; i++) {
                    int c = assignment[i];
                    size[c] += 1;
                }
                for (int i = 0; i < assignment.length; i++) {
                    int ci = assignment[i];
                    for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                        int j = e.getKey();
                        if (i <= j && assignment[j] == ci) {
                            internal[ci] += e.getValue();
                        }
                    }
                }

                double q = 0d;
                for (int c = 0; c < communities; c++) {
                    int n_c = size[c];
                    if (n_c <= 1) {
                        // pairs term is zero; internal already accounts for self-loops if any
                        q += internal[c];
                    } else {
                        q += internal[c] - resolution * (n_c * (n_c - 1)) / 2.0;
                    }
                }
                return q;
            }
        }

        // ------------------------ Leiden Phase 3: aggregation ------------------------

        Level aggregate(int[] assignment, int communityCount)
        {
            List<Map<Integer, Double>> newAdjacency = new ArrayList<>(communityCount);
            for (int i = 0; i < communityCount; i++) newAdjacency.add(new HashMap<>());
            double[] newDegree = new double[communityCount];
            double[] newSelfLoops = new double[communityCount];

            for (int i = 0; i < assignment.length; i++) {
                int c = assignment[i];
                newDegree[c] += nodeDegree[i];
            }

            for (int i = 0; i < assignment.length; i++) {
                int ci = assignment[i];
                for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                    int j = e.getKey();
                    if (i > j) continue;
                    double w = e.getValue();
                    int cj = assignment[j];
                    if (ci == cj) {
                        newSelfLoops[ci] += w;
                    } else {
                        newAdjacency.get(ci).merge(cj, w, Double::sum);
                        newAdjacency.get(cj).merge(ci, w, Double::sum);
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

        // ------------------------ Helpers ------------------------

        private boolean isEdgeless() { return m2 < EPSILON; }

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

    // =====================================================================

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
