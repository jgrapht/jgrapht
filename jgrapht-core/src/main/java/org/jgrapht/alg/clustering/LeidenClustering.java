/*
 * (C) 2025 Your Name and Contributors.
 * JGraphT: Leiden community detection (clean, readable, full-feature implementation)
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
 * <p>Implements the 3-phase Leiden procedure:
 * <ol>
 *   <li><b>Local moving</b>: move individual nodes to neighboring communities to improve quality
 *   <li><b>Refinement</b>: split communities that are not well-connected (here: disconnected parts)
 *   <li><b>Aggregation</b>: collapse each community to a super-node; repeat until no improvement
 * </ol>
 *
 * <p>Supports two quality functions via {@link Quality}:
 * <ul>
 *   <li>{@link Quality#MODULARITY}: standard (Louvain-like) modularity with resolution γ
 *   <li>{@link Quality#CPM}: Constant Potts Model. The local gain uses a practical weighted form
 *       ΔQ = k_i,in(C) − γ · strength(i). This favors small, dense communities and avoids the
 *       modularity resolution limit.
 * </ul>
 *
 * <p><b>Notes on refinement:</b> Leiden’s full refinement defines “well-connectedness”. For clarity
 * and robustness, this implementation guarantees at minimum that each community’s induced subgraph
 * is (edge-)connected. Disconnected parts are split into separate subcommunities before aggregation,
 * which is the essential improvement over Louvain.
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
        /** Modularity with resolution γ. */
        MODULARITY,
        /** Constant Potts Model (weighted variant). */
        CPM
    }

    private final Graph<V, E> graph;
    private final double resolution;
    private final Quality quality;
    private final Random rng;

    private Clustering<V> cached;

    /**
     * Create Leiden with default γ=1.0, quality=MODULARITY and a default RNG.
     */
    public LeidenClustering(Graph<V, E> graph)
    {
        this(graph, 1.0, new Random(), Quality.MODULARITY);
    }

    /**
     * Create Leiden with custom resolution γ, default RNG, quality=MODULARITY.
     */
    public LeidenClustering(Graph<V, E> graph, double resolution)
    {
        this(graph, resolution, new Random(), Quality.MODULARITY);
    }

    /**
     * Create Leiden with custom γ, RNG and quality.
     *
     * @param graph undirected input graph (weighted or unweighted)
     * @param resolution positive resolution γ (modularity/CPM)
     * @param rng random source used to shuffle vertex order
     * @param quality quality function to optimize
     */
    public LeidenClustering(Graph<V, E> graph, double resolution, Random rng, Quality quality)
    {
        this.graph = GraphTests.requireUndirected(graph);
        if (!(resolution > 0) || Double.isNaN(resolution) || Double.isInfinite(resolution)) {
            throw new IllegalArgumentException("Resolution γ must be a positive, finite number");
        }
        this.resolution = resolution;
        this.rng = Objects.requireNonNull(rng, "rng");
        this.quality = Objects.requireNonNull(quality, "quality");
    }

    @Override
    public Clustering<V> getClustering()
    {
        if (cached == null) {
            List<Set<V>> clusters = new Impl<>(graph, resolution, quality, rng).run();
            cached = new ClusteringImpl<>(clusters);
        }
        return cached;
    }

    /* ========================== Internal implementation ========================== */

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
         * Full Leiden loop: keep creating aggregated levels while quality improves.
         * We remember assignments per level and compose them at the end.
         */
        List<Set<V>> run()
        {
            // Map vertices to indices [0..n-1] for fast arrays.
            List<V> vs = new ArrayList<>(g.vertexSet());
            if (vs.isEmpty())
                return Collections.emptyList();

            Map<V, Integer> idx = new HashMap<>();
            for (int i = 0; i < vs.size(); i++) idx.put(vs.get(i), i);

            Level level = Level.fromGraph(g, vs, idx);//Level object that represents the graph in array form
            double bestScore = Double.NEGATIVE_INFINITY;//keeps track of the best quality value (modularity or CPM score) seen so far.

            List<int[]> levelAssignments = new ArrayList<>();

            while (true) {
                // 1) Local moving
                int[] community = level.localMoving(gamma, quality, rng);

                // 2) Refinement (split disconnected parts in each community)
                community = level.refineDisconnected(community);

                // Compute current quality
                double score = level.computeQuality(community, gamma, quality);

                // Stop if no improvement
                if (score <= bestScore + EPS) {
                    break;
                }

                bestScore = score;
                levelAssignments.add(community);

                // 3) Aggregation: form next level graph; stop if no compression
                Level.AggregateResult agg = level.aggregate(community);
                if (agg.communityCount == level.size()) {
                    // cannot compress further,number of communities equals the number of nodes at this level
                    break;
                }
                level = agg.nextLevel;
            }

            // Compose per-level assignments into a final partition over original vertices.
            return buildClusters(vs, levelAssignments);
        }

        /** Compose level assignments and return vertex sets. */
        private List<Set<V>> buildClusters(List<V> vs, List<int[]> assignments)
        {
            if (assignments.isEmpty()) {
                // return singletons
                List<Set<V>> res = new ArrayList<>(vs.size());
                for (V v : vs) res.add(new LinkedHashSet<>(Collections.singleton(v)));
                return res;
            }
            int n = vs.size();
            int[] finalComm = new int[n];
            for (int v = 0; v < n; v++) {
                int c = assignments.get(0)[v];
                for (int lvl = 1; lvl < assignments.size(); lvl++) {
                    c = assignments.get(lvl)[c];
                }
                finalComm[v] = c;
            }
            // group by community id (preserve insertion order)
            Map<Integer, Set<V>> map = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                map.computeIfAbsent(finalComm[i], k -> new LinkedHashSet<>()).add(vs.get(i));
            }
            return new ArrayList<>(map.values());
        }
    }

    /**
     * One Leiden level: compact weighted adjacency, degrees, and total weight (2m).
     * All arrays/counts are in the index space [0..n-1] of this level.
     */
    private static final class Level
    {
        /** For each node u, adjacency[u] is a map (v -> weight). Includes self-loops if present. */
        private final List<Map<Integer, Double>> adjacency;
        /** Node strength (weighted degree). */
        private final double[] strength;
        /** Twice the total edge weight 2m (sum of strengths). */
        private final double m2;

        private Level(List<Map<Integer, Double>> adjacency, double[] strength, double m2)
        {
            this.adjacency = adjacency;
            this.strength = strength;
            this.m2 = m2;
        }

        int size() { return strength.length; }

        /** Build level-0 graph from JGraphT graph. */
        static <V, E> Level fromGraph(Graph<V, E> g, List<V> vs, Map<V, Integer> index)
        {
            final int n = vs.size();
            List<Map<Integer, Double>> adj = new ArrayList<>(n);
            for (int i = 0; i < n; i++) adj.add(new HashMap<>());

            double[] deg = new double[n];
            double total = 0d;
            boolean weighted = g.getType().isWeighted();

            for (E e : g.edgeSet()) {
                int u = index.get(g.getEdgeSource(e));
                int v = index.get(g.getEdgeTarget(e));
                double w = weighted ? g.getEdgeWeight(e) : 1.0;
                if (!(w >= 0) || Double.isNaN(w))
                    throw new IllegalArgumentException("Negative/NaN edge weight");
                if (u == v) {
                    // self-loop contributes 2w to degree sum for undirected modularity (m2 sum)
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
         * Local moving: in random order, move node i to the neighboring community which maximizes
         * the quality gain (depending on {@link Quality}).
         *
         * @return community assignment array of length n (labels may be sparse; use refine/aggregate after)
         */
        int[] localMoving(double gamma, Quality q, Random rng)
        {
            final int n = size();
            int[] comm = new int[n];
            double[] commStrength = new double[n];
            for (int i = 0; i < n; i++) {
                comm[i] = i;               // each node in its own community initially
                commStrength[i] = strength[i]; // so each community strength = node strength

            }

            if (m2 < EPS) {
                // No edges — keep singletons
                return comm;
            }

            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;

            boolean moved;
            do {
                moved = false;
                shuffle(order, rng);

                for (int u : order) {
                    double k_u = strength[u];
                    if (k_u < EPS) continue;

                    int cu = comm[u];

                    // Temporarily remove u from its community
                    commStrength[cu] -= k_u;

                    // Gather weights to each neighboring community
                    Map<Integer, Double> weightToComm = new HashMap<>();
                    for (Entry<Integer, Double> e : adjacency.get(u).entrySet()) {
                        int v = e.getKey();
                        double w = e.getValue();
                        int cv = comm[v];
                        weightToComm.merge(cv, w, Double::sum);
                    }
                    // Ensure current community is considered (even if u had no intra edges)
                    weightToComm.putIfAbsent(cu, 0d);

                    int bestC = cu;
                    double bestGain = 0;
                    double tieBreaker = weightToComm.get(cu);

                    for (Entry<Integer, Double> e : weightToComm.entrySet()) {
                        int c = e.getKey();
                        double k_u_in = e.getValue();          // sum of weights from u to community c
                        double gain = qualityGain(q, gamma, k_u, k_u_in, commStrength[c]);

                        if (gain >= bestGain - EPS ||
                            (Math.abs(gain - bestGain) <= EPS && k_u_in > tieBreaker + EPS)) {
                            bestGain = gain;
                            tieBreaker = k_u_in;
                            bestC = c;
                        }
                    }

                    // Move u to bestC
                    commStrength[bestC] += k_u;
                    if (bestC != cu) {
                        comm[u] = bestC;
                        moved = true;
                    }
                }
            } while (moved);

            // Compress community labels to [0..C-1]
            return relabelCommunities(comm);
        }

        /**
         * Quality gain ΔQ when moving a node u (strength k_u) to community C:
         * <pre>
         * MODULARITY: ΔQ = k_u,in(C) − γ * (k_u * sumStrength(C) / m2)
         * CPM       : ΔQ = k_u,in(C) − γ * k_u
         * </pre>
         */
        private double qualityGain(Quality q, double gamma, double k_u, double k_u_in, double sumC)
        {
            if (q == Quality.MODULARITY) {
                return k_u_in - gamma * (k_u * sumC) / Math.max(m2, EPS);
            } else { // CPM (weighted practical form)
                return k_u_in - gamma * k_u;
            }
        }

        /* --------------------------- Phase 2: Refinement --------------------------- */

        /**
         * Split each community into connected components of its induced subgraph.
         * Returns a new assignment with communities relabeled and possibly increased in count.
         */
        int[] refineDisconnected(int[] comm)
        {
            final int n = size();
            // Build members per community id found in 'comm'
            Map<Integer, List<Integer>> members = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) members.computeIfAbsent(comm[i], k -> new ArrayList<>()).add(i);

            int nextId = 0;
            int[] refined = new int[n];

            // For each community, split by connected components using BFS over adjacency filtered to members
            for (List<Integer> group : members.values()) {
                // quick set membership check
                Set<Integer> inGroup = new HashSet<>(group);

                // mark unvisited
                Set<Integer> unvisited = new LinkedHashSet<>(group);
                while (!unvisited.isEmpty()) {
                    int seed = unvisited.iterator().next();
                    unvisited.remove(seed);

                    // BFS component
                    Deque<Integer> dq = new ArrayDeque<>();
                    dq.add(seed);
                    refined[seed] = nextId;

                    while (!dq.isEmpty()) {
                        int u = dq.removeFirst();
                        for (Entry<Integer, Double> e : adjacency.get(u).entrySet()) {
                            if (e.getValue() <= EPS) continue;
                            int v = e.getKey();
                            if (inGroup.contains(v) && unvisited.remove(v)) {
                                refined[v] = nextId;
                                dq.addLast(v);
                            }
                        }
                    }
                    nextId++; // next component label
                }
            }
            return refined;
        }

        /* --------------------------- Phase 3: Aggregation --------------------------- */

        /**
         * Aggregate by turning each community into a super-node and summing inter-community edge weights.
         * Self-loops store intra-community weights. Degrees and total weight are preserved.
         */
        AggregateResult aggregate(int[] comm)
        {
            // Map old community ids to [0..C-1]
            int[] relabeled = relabelCommunities(comm);
            int C = 0;
            for (int x : relabeled) C = Math.max(C, x + 1);

            List<Map<Integer, Double>> newAdj = new ArrayList<>(C);
            for (int i = 0; i < C; i++) newAdj.add(new HashMap<>());
            double[] newStrength = new double[C];

            // sum strengths per community
            for (int i = 0; i < relabeled.length; i++) newStrength[relabeled[i]] += strength[i];

            // Build edges between super-nodes
            for (int i = 0; i < relabeled.length; i++) {
                int ci = relabeled[i];
                for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                    int j = e.getKey();
                    double w = e.getValue();
                    if (i > j) continue; // handle each undirected pair once
                    int cj = relabeled[j];
                    if (ci == cj) {
                        // intra-community: becomes self-loop
                        newAdj.get(ci).merge(ci, w, Double::sum);
                    } else {
                        newAdj.get(ci).merge(cj, w, Double::sum);
                        newAdj.get(cj).merge(ci, w, Double::sum);
                    }
                }
            }

            return new AggregateResult(new Level(newAdj, newStrength, m2), C);
        }

        /* --------------------------- Utilities --------------------------- */

        /** Compute overall quality of a partition for reporting/selection. */
        double computeQuality(int[] comm, double gamma, Quality q)
        {
            if (comm.length == 0 || m2 < EPS) return 0d;

            int C = 0;
            for (int x : comm) C = Math.max(C, x + 1);

            double[] sumStrength = new double[C];
            double[] internal = new double[C]; // sum of internal weights (count each undirected edge once)

            for (int i = 0; i < comm.length; i++) {
                int ci = comm[i];
                sumStrength[ci] += strength[i];
                for (Entry<Integer, Double> e : adjacency.get(i).entrySet()) {
                    int j = e.getKey();
                    if (i <= j && comm[j] == ci) {
                        internal[ci] += e.getValue();
                    }
                }
            }

            double qsum = 0d;
            if (q == Quality.MODULARITY) {
                for (int c = 0; c < C; c++) {
                    double in = internal[c];     // total weight of intra edges (counted once)
                    double tot = sumStrength[c]; // sum of node strengths in c
                    qsum += (in / m2) - gamma * (tot / m2) * (tot / m2);
                }
            } else { // CPM (weighted practical form)
                for (int c = 0; c < C; c++) {
                    double in = internal[c];
                    double tot = sumStrength[c];
                    // With ΔQ choice used in local moving, a consistent global score is:
                    qsum += in - gamma * tot / 2.0; // factor 1/2 because strengths sum to 2m
                }
                // normalize by m (optional). We keep raw CPM here for monotonic comparison.
            }
            return qsum;
        }

        /** Relabel community ids to dense [0..C-1]. */
        private static int[] relabelCommunities(int[] comm)
        {
            Map<Integer, Integer> map = new LinkedHashMap<>();
            int next = 0;
            int[] out = new int[comm.length];
            for (int i = 0; i < comm.length; i++) {
                int c = comm[i];
                Integer m = map.get(c);
                if (m == null) { m = next++; map.put(c, m); }
                out[i] = m;
            }
            return out;
        }

        //randomizes node order during local moving(Fisher–Yates shuffle)
        private static void shuffle(int[] a, Random rng)
        {
            for (int i = a.length - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int t = a[i]; a[i] = a[j]; a[j] = t;
            }
        }

        /** Holder for aggregation result. */
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
