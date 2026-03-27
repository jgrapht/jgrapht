/*
 * (C) Copyright 2019-2024, by xiangyu MAO , Hanine Gharsalli and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

import java.util.*;
import java.util.Map.Entry;

/**
 * Leiden community detection for undirected graphs (weighted or unweighted).
 *
 * Implements the 3-phase Leiden procedure:
 * 1) Local moving
 * 2) Refinement (split disconnected components)
 * 3) Aggregation (collapse communities)
 *
 * Supports MODULARITY and CPM objective functions.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LeidenClustering<V, E> implements ClusteringAlgorithm<V>
{
    /** Numerical epsilon for floating-point comparisons. */
    private static final double EPS = 1e-12;

    /** Quality function options for Leiden. */
    public enum Quality {
        MODULARITY,
        CPM
    }

    private final Graph<V, E> graph;
    private final double resolution;
    private final Quality quality;
    private final Random rng;

    /** Cache final clustering once computed */
    private Clustering<V> cached;

    /**
     * Constructor with γ = 1.0, MODULARITY, and default RNG.
     *
     * @param graph input graph
     */
    public LeidenClustering(Graph<V, E> graph)
    {
        this(graph, 1.0, new Random(), Quality.MODULARITY);
    }

    /**
     * Constructor with custom γ (resolution), MODULARITY, and default RNG.
     *
     * @param graph input graph
     * @param resolution resolution parameter γ
     */
    public LeidenClustering(Graph<V, E> graph, double resolution)
    {
        this(graph, resolution, new Random(), Quality.MODULARITY);
    }

    /**
     * Full constructor.
     *
     * @param graph undirected input graph
     * @param resolution positive resolution γ
     * @param rng random generator used for node ordering
     * @param quality objective function (MODULARITY or CPM)
     */
    public LeidenClustering(Graph<V, E> graph, double resolution, Random rng, Quality quality)
    {
        this.graph = GraphTests.requireUndirected(graph);
        if (!(resolution > 0) || Double.isNaN(resolution) || Double.isInfinite(resolution)) {
            throw new IllegalArgumentException("Resolution γ must be positive and finite");
        }
        this.resolution = resolution;
        this.rng = Objects.requireNonNull(rng);
        this.quality = Objects.requireNonNull(quality);
    }

    /**
     * Compute or return cached Leiden clustering result.
     *
     * @return JGraphT clustering: list of vertex sets
     */
    @Override
    public Clustering<V> getClustering()
    {
        if (cached == null) {
            List<Set<V>> clusters = new Impl<>(graph, resolution, quality, rng).run();
            cached = new ClusteringImpl<>(clusters);
        }
        return cached;
    }

    /* ========================== Internal Implementation ========================== */

    /**
     * Internal class performing all Leiden steps on index-based arrays.
     */
    private static final class Impl<V, E>
    {
        private final Graph<V, E> g;
        private final double gamma;
        private final Quality quality;
        private final Random rng;

        Impl(Graph<V, E> g, double gamma, Quality quality, Random rng)
        {
            this.g = g;
            this.gamma = gamma;
            this.quality = quality;
            this.rng = rng;
        }

        /**
         * Run full multi-level Leiden algorithm.
         *
         * Steps repeated:
         * 1) local moving
         * 2) refinement of disconnected components
         * 3) aggregation
         *
         * Stops when modularity/CPM no longer improves.
         *
         * @return list of vertex clusters in original vertex space
         */
        List<Set<V>> run()
        {
            // Map vertices to indices
            List<V> vs = new ArrayList<>(g.vertexSet());
            if (vs.isEmpty()) return Collections.emptyList();

            Map<V, Integer> idx = new HashMap<>();
            for (int i = 0; i < vs.size(); i++) idx.put(vs.get(i), i);

            // Build first-level graph
            Level level = Level.fromGraph(g, vs, idx);
            double bestScore = Double.NEGATIVE_INFINITY;

            // Track the community assignment of each level
            List<int[]> levelAssignments = new ArrayList<>();

            while (true) {
                // 1) local moving
                int[] community = level.localMoving(gamma, quality, rng);

                // 2) refinement
                community = level.refineDisconnected(community);

                // 3) compute quality
                double score = level.computeQuality(community, gamma, quality);

                if (score <= bestScore + EPS) break;
                bestScore = score;
                levelAssignments.add(community);

                // 4) aggregation
                Level.AggregateResult agg = level.aggregate(community);

                // if no compression: stop
                if (agg.communityCount == level.size()) break;

                level = agg.nextLevel;
            }

            // Expand hierarchical assignments to clusters
            return buildClusters(vs, levelAssignments);
        }

        /**
         * Compose multi-level assignments into final vertex→community mapping.
         *
         * @param vs list of original vertices
         * @param assignments list of int[] assignments per level
         * @return list of vertex sets (clusters)
         */
        private List<Set<V>> buildClusters(List<V> vs, List<int[]> assignments)
        {
            if (assignments.isEmpty()) {
                // no improvement: each vertex alone
                List<Set<V>> res = new ArrayList<>(vs.size());
                for (V v : vs) res.add(new LinkedHashSet<>(Collections.singleton(v)));
                return res;
            }

            int n = vs.size();
            int[] finalComm = new int[n];

            // Combine levels: comm[v] = A2[A1[v]]...
            for (int v = 0; v < n; v++) {
                int c = assignments.get(0)[v];
                for (int lvl = 1; lvl < assignments.size(); lvl++) {
                    c = assignments.get(lvl)[c];
                }
                finalComm[v] = c;
            }

            // Group by community
            Map<Integer, Set<V>> map = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                map.computeIfAbsent(finalComm[i], k -> new LinkedHashSet<>()).add(vs.get(i));
            }
            return new ArrayList<>(map.values());
        }
    }

    /**
     * A "Level" stores the graph of one Leiden iteration:
     * - compact adjacency list with weights
     * - node strengths
     * - total edge weight
     *
     * All nodes are indexed 0..n-1.
     */
    private static final class Level
    {
        /** adjacency[u]: map v→weight (undirected, includes self-loops) */
        private final List<Map<Integer, Double>> adjacency;
        /** node strength (weighted degree) */
        private final double[] strength;
        /** total weight 2m (sum of all strengths) */
        private final double m2;

        private Level(List<Map<Integer, Double>> adjacency, double[] strength, double m2)
        {
            this.adjacency = adjacency;
            this.strength = strength;
            this.m2 = m2;
        }

        /** @return number of nodes in this level */
        int size() { return strength.length; }

        /**
         * Build Level 0 (initial level) from JGraphT graph.
         *
         * @param g input graph
         * @param vs vertex list
         * @param index mapping V → int index
         * @return Level object with adjacency and weights
         */
        static <V, E> Level fromGraph(Graph<V, E> g, List<V> vs, Map<V, Integer> index)
        {
            final int n = vs.size();
            List<Map<Integer, Double>> adj = new ArrayList<>(n);
            for (int i = 0; i < n; i++) adj.add(new HashMap<>());

            double[] deg = new double[n];
            double total = 0;
            boolean weighted = g.getType().isWeighted();

            for (E e : g.edgeSet()) {
                int u = index.get(g.getEdgeSource(e));
                int v = index.get(g.getEdgeTarget(e));
                double w = weighted ? g.getEdgeWeight(e) : 1.0;

                if (!(w >= 0)) throw new IllegalArgumentException("Negative or NaN edge weight");

                if (u == v) {
                    // self-loop contributes twice
                    adj.get(u).merge(v, w, Double::sum);
                    deg[u] += 2 * w;
                    total += 2 * w;
                } else {
                    adj.get(u).merge(v, w, Double::sum);
                    adj.get(v).merge(u, w, Double::sum);
                    deg[u] += w;
                    deg[v] += w;
                    total += 2 * w;
                }
            }

            return new Level(adj, deg, total);
        }

        /* --------------------------- Phase 1: Local moving --------------------------- */

        /**
         * Local moving phase:
         * Each node is visited in random order and moved to the neighboring community that
         * maximizes ΔQ.
         *
         * INPUT:
         *  @param gamma resolution parameter γ
         *  @param q quality function (MODULARITY or CPM)
         *  @param rng random source
         *
         * OUTPUT:
         *  @return community assignment int[] of size n
         */
        int[] localMoving(double gamma, Quality q, Random rng)
        {
            final int n = size();
            int[] comm = new int[n];
            double[] commStrength = new double[n];

            // Initial: each node in its own community
            for (int i = 0; i < n; i++) {
                comm[i] = i;
                commStrength[i] = strength[i];
            }

            if (m2 < EPS) return comm;

            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;

            boolean moved;
            do {
                moved = false;
                shuffle(order, rng);

                for (int u : order) {
                    double ku = strength[u];
                    if (ku < EPS) continue;

                    int cu = comm[u];
                    commStrength[cu] -= ku;

                    // collect weight from u to each community
                    Map<Integer, Double> weightToComm = new HashMap<>();
                    for (Entry<Integer, Double> e : adjacency.get(u).entrySet()) {
                        int v = e.getKey();
                        double w = e.getValue();
                        int cv = comm[v];
                        weightToComm.merge(cv, w, Double::sum);
                    }

                    weightToComm.putIfAbsent(cu, 0d);

                    int bestC = cu;
                    double bestGain = 0;
                    double tieBreaker = weightToComm.get(cu);

                    for (Entry<Integer, Double> e : weightToComm.entrySet()) {
                        int c = e.getKey();
                        double kuIn = e.getValue();

                        double gain = qualityGain(q, gamma, ku, kuIn, commStrength[c]);

                        if (gain >= bestGain - EPS ||
                            (Math.abs(gain - bestGain) <= EPS && kuIn > tieBreaker + EPS)) {
                            bestC = c;
                            bestGain = gain;
                            tieBreaker = kuIn;
                        }
                    }

                    commStrength[bestC] += ku;
                    if (bestC != cu) {
                        comm[u] = bestC;
                        moved = true;
                    }
                }
            } while (moved);

            return relabelCommunities(comm);
        }

        /**
         * Compute ΔQ for moving node u to community C.
         *
         * @param q quality metric
         * @param gamma resolution
         * @param ku strength of node u
         * @param kuIn weight from u to C
         * @param sumC strength of community C
         * @return quality improvement (ΔQ)
         */
        private double qualityGain(Quality q, double gamma, double ku, double kuIn, double sumC)
        {
            if (q == Quality.MODULARITY)
                return kuIn - gamma * (ku * sumC) / Math.max(m2, EPS);
            else
                return kuIn - gamma * ku; // CPM
        }

        /* --------------------------- Phase 2: Refinement --------------------------- */

        /**
         * REFINE: Splits each community into connected components of its induced subgraph.
         *
         * INPUT:
         *  @param comm community assignment
         *
         * OUTPUT:
         *  @return refined community assignment with potentially more communities
         */
        int[] refineDisconnected(int[] comm)
        {
            final int n = size();
            Map<Integer, List<Integer>> members = new LinkedHashMap<>();

            for (int i = 0; i < n; i++)
                members.computeIfAbsent(comm[i], k -> new ArrayList<>()).add(i);

            int nextId = 0;
            int[] refined = new int[n];

            for (List<Integer> group : members.values()) {
                Set<Integer> inGroup = new HashSet<>(group);
                Set<Integer> unvisited = new LinkedHashSet<>(group);

                while (!unvisited.isEmpty()) {
                    int seed = unvisited.iterator().next();
                    unvisited.remove(seed);

                    Deque<Integer> dq = new ArrayDeque<>();
                    dq.add(seed);
                    refined[seed] = nextId;

                    while (!dq.isEmpty()) {
                        int u = dq.removeFirst();
                        for (Entry<Integer, Double> e : adjacency.get(u).entrySet()) {
                            int v = e.getKey();
                            if (e.getValue() > EPS &&
                                inGroup.contains(v) &&
                                unvisited.remove(v)) {
                                refined[v] = nextId;
                                dq.addLast(v);
                            }
                        }
                    }
                    nextId++;
                }
            }
            return refined;
        }

        /* --------------------------- Phase 3: Aggregation --------------------------- */

        /**
         * AGGREGATION: collapses each community into a super-node and reconstructs adjacency.
         *
         * INPUT:
         *  @param comm community assignment
         *
         * OUTPUT:
         *  @return AggregateResult containing next-level graph and number of communities
         */
        AggregateResult aggregate(int[] comm)
        {
            int[] relabeled = relabelCommunities(comm);
            int c = 0;
            for (int x : relabeled) c = Math.max(c, x + 1);

            List<Map<Integer, Double>> newAdj = new ArrayList<>(c);
            for (int i = 0; i < c; i++) newAdj.add(new HashMap<>());

            double[] newStrength = new double[c];
            for (int i = 0; i < relabeled.length; i++)
                newStrength[relabeled[i]] += strength[i];

            // sum edges between supernodes
            for (int i = 0; i < relabeled.length; i++) {
                int ci = relabeled[i];
                for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                    int j = e.getKey();
                    double w = e.getValue();
                    if (i > j) continue;
                    int cj = relabeled[j];

                    if (ci == cj)
                        newAdj.get(ci).merge(ci, w, Double::sum);
                    else {
                        newAdj.get(ci).merge(cj, w, Double::sum);
                        newAdj.get(cj).merge(ci, w, Double::sum);
                    }
                }
            }

            return new AggregateResult(new Level(newAdj, newStrength, m2), c);
        }

        /* --------------------------- Utility Methods --------------------------- */

        /**
         * Compute global quality score for MODULARITY or CPM.
         *
         * @param comm community assignment
         * @param gamma resolution
         * @param q quality function
         * @return quality score (higher = better)
         */
        double computeQuality(int[] comm, double gamma, Quality q)
        {
            if (comm.length == 0 || m2 < EPS) return 0;

            int c = 0;
            for (int x : comm) c = Math.max(c, x + 1);

            double[] sumStrength = new double[c];
            double[] internal = new double[c];

            for (int i = 0; i < comm.length; i++) {
                int ci = comm[i];
                sumStrength[ci] += strength[i];
                for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                    int j = e.getKey();
                    if (i <= j && comm[j] == ci)
                        internal[ci] += e.getValue();
                }
            }

            double qsum = 0;
            if (q == Quality.MODULARITY) {
                for (int i = 0; i < c; i++) {
                    double in = internal[i];
                    double tot = sumStrength[i];
                    qsum += (in / m2) - gamma * (tot / m2) * (tot / m2);
                }
            } else { // CPM
                for (int i = 0; i < c; i++) {
                    double in = internal[i];
                    double tot = sumStrength[i];
                    qsum += in - gamma * tot / 2.0;
                }
            }
            return qsum;
        }

        /**
         * Renumber community labels to consecutive integers 0..C-1.
         *
         * @param comm original labels
         * @return compact relabeled array
         */
        private static int[] relabelCommunities(int[] comm)
        {
            Map<Integer, Integer> map = new LinkedHashMap<>();
            int next = 0;

            int[] out = new int[comm.length];
            for (int i = 0; i < comm.length; i++) {
                map.putIfAbsent(comm[i], next++);
                out[i] = map.get(comm[i]);
            }
            return out;
        }

        /**
         * Fisher–Yates shuffle of int array.
         *
         * @param a array to shuffle
         * @param rng random generator
         */
        private static void shuffle(int[] a, Random rng)
        {
            for (int i = a.length - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
        }

        /** Result wrapper for aggregation. */
        static final class AggregateResult {
            final Level nextLevel;
            final int communityCount;

            AggregateResult(Level nextLevel, int communityCount) {
                this.nextLevel = nextLevel;
                this.communityCount = communityCount;
            }
        }
    }
}
