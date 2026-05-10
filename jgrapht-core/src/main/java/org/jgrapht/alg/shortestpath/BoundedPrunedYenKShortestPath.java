/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Experimental bounded-pruned variant of Yen's $k$ shortest loopless paths algorithm.
 *
 * <p>
 * This class returns the same ordered sequence of path weights as {@link YenKShortestPath} (and is
 * regression-tested against it) but tries to perform fewer spur shortest-path computations and
 * fewer candidate materializations by deferring spur work behind <em>lower-bound certificates</em>.
 *
 * <h3>Key idea</h3>
 *
 * Standard Yen, when accepting a path $P$, eagerly enumerates every spur index of $P$, runs a
 * shortest-path query under the appropriate banning, materializes the full candidate path, and
 * pushes it into the candidate heap. For large $k$ on dense graphs this generates many candidates
 * that will never be output.
 *
 * <p>
 * The bounded-pruned variant defers each spur enumeration as a {@code SpurTask} carrying:
 * <ul>
 *   <li>the parent accepted path,</li>
 *   <li>the spur index in that parent,</li>
 *   <li>the banned vertex set (the parent's prefix),</li>
 *   <li>the root cost so far,</li>
 *   <li>and a <em>lower bound</em> on any candidate it could ever produce:
 *       {@code lowerBound = rootCost + reverseDistanceToSink[spurNode]}.</li>
 * </ul>
 *
 * <p>
 * Reverse distances are computed once on the original graph. Because removing vertices/edges can
 * only increase shortest-path distance, those reverse distances remain valid lower bounds even
 * under arbitrary bans. So:
 *
 * <blockquote>If the cheapest materialized candidate already satisfies
 * {@code candidate.cost ≤ task.lowerBound} for every still-deferred task, it is safe to output
 * the candidate as the next path — no deferred task could possibly produce something better.
 * </blockquote>
 *
 * <h3>Algorithm sketch</h3>
 *
 * <pre>
 *   firstPath = shortestPath(source, sink)
 *   accepted.add(firstPath)
 *   addSpurTasks(firstPath)
 *   while accepted.size &lt; k:
 *       while taskHeap not empty AND
 *             (candidateHeap empty OR taskHeap.minLB &lt; candidateHeap.minCost):
 *           materialize(taskHeap.pop())  // run the actual spur query, push candidate
 *       if candidateHeap empty: break
 *       next = candidateHeap.pop()
 *       accepted.add(next)
 *       addSpurTasks(next)
 * </pre>
 *
 * <h3>Exactness</h3>
 *
 * The returned path-weight sequence equals that of {@link YenKShortestPath}. Tie-breaking among
 * paths of equal weight is deterministic: a stable ordinal is attached to every candidate / task
 * push so that two equal weights are broken by insertion order, which mirrors how the standard
 * implementation eagerly inserts candidates as it accepts each path. The benchmark harness
 * verifies the path-weight sequences match exactly across many graph families.
 *
 * <h3>What this class does NOT yet claim</h3>
 *
 * <ul>
 *   <li>No proven better worst-case complexity: Yen's $O(k\,n\,(m + n \log n))$ remains the
 *       upper bound. The bounded-pruned layer is an output-sensitive optimisation only.</li>
 * </ul>
 *
 * <h3>References</h3>
 *
 * <ul>
 *   <li>Yen, J. Y. (1971). Finding the k shortest loopless paths in a network.
 *       <i>Management Science</i>, 17(11), 712–716. The original loopless k-shortest-paths
 *       algorithm whose spur step is reused here.</li>
 *   <li>Martins, E. Q. V., &amp; Pascoal, M. M. B. (2003). A new implementation of Yen's
 *       ranking loopless paths algorithm. <i>4OR — Quarterly Journal of the Belgian, French
 *       and Italian Operations Research Societies</i>, 1(2), 121–133. Practical
 *       implementation notes that informed JGraphT's existing {@link YenKShortestPath}.</li>
 *   <li>Aljazzar, H., &amp; Leue, S. (2011). K*: A heuristic search algorithm for finding the
 *       k shortest paths. <i>Artificial Intelligence</i>, 175(18), 2129–2154. The closest
 *       prior published precedent for using admissible lower bounds (an A*-style heuristic on
 *       the reverse graph) to defer expansion of provably non-improving k-paths candidates.
 *       This class applies the same lower-bound-then-defer idea at the Yen spur-task level
 *       rather than at K*'s edge-expansion level.</li>
 * </ul>
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class BoundedPrunedYenKShortestPath<V, E>
    implements KShortestPathAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private final SpurShortestPathEngine<V, E> engine;

    private final Stats stats = new Stats();

    /**
     * Default constructor using {@link DijkstraSpurEngine} as the spur shortest-path back-end.
     * This default mirrors the spur step of the existing {@link YenKShortestPath} for
     * principle-of-least-surprise. Pass an {@link AStarSpurEngine} via
     * {@link #BoundedPrunedYenKShortestPath(Graph, SpurShortestPathEngine)} to opt into the
     * reverse-distance A* heuristic — recommended for dense graphs or large k. The A* engine
     * is exact (the heuristic is admissible by construction).
     *
     * @param graph the input graph (must not be {@code null})
     */
    public BoundedPrunedYenKShortestPath(Graph<V, E> graph)
    {
        this(graph, new DijkstraSpurEngine<>());
    }

    /**
     * Constructor with a pluggable {@link SpurShortestPathEngine}.
     *
     * @param graph the input graph (must not be {@code null})
     * @param engine the spur shortest-path engine to use (must not be {@code null})
     */
    public BoundedPrunedYenKShortestPath(
        Graph<V, E> graph, SpurShortestPathEngine<V, E> engine)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
        this.engine = Objects.requireNonNull(engine, "Engine cannot be null!");
    }

    @Override
    public List<GraphPath<V, E>> getPaths(V source, V sink, int k)
    {
        if (k < 0) {
            throw new IllegalArgumentException("k should be positive");
        }
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph should contain source vertex!");
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException("Graph should contain sink vertex!");
        }
        stats.reset();
        engine.resetCounters();

        if (k == 0) {
            return new ArrayList<>();
        }

        // -- Step 1: reverse distances from sink (single Dijkstra on edge-reversed original graph)
        Map<V, Double> reverseDistances = computeReverseDistances(sink);

        // -- Step 2: first shortest path
        GraphPath<V, E> first = engine.findPath(
            graph, source, sink, Collections.emptySet(), Collections.emptySet(), reverseDistances);
        if (first == null) {
            return new ArrayList<>();
        }

        return runBoundedLoop(first, sink, k, reverseDistances);
    }

    private List<GraphPath<V, E>> runBoundedLoop(
        GraphPath<V, E> first, V sink, int k, Map<V, Double> reverseDistances)
    {
        List<GraphPath<V, E>> accepted = new ArrayList<>();
        // Per-path metadata: index of first deviation vertex from parent (source for the first
        // shortest path).
        List<Integer> firstDeviationIndex = new ArrayList<>();

        // Heaps. We use a stable ordinal as a tiebreaker so identical-weight items break ties by
        // insertion order — matches standard Yen's behaviour closely.
        PriorityQueue<SpurTask> taskHeap = new PriorityQueue<>();
        PriorityQueue<Candidate> candHeap = new PriorityQueue<>();
        // Dedup: a (vertex-list) signature so we never push the same candidate twice. Candidate
        // duplication can in principle occur even under banning if multiple parents lead to the
        // same path through different spur indices.
        Set<List<V>> seenCandidates = new HashSet<>();

        accepted.add(first);
        firstDeviationIndex.add(0); // first path deviates at source
        seenCandidates.add(first.getVertexList());
        addSpurTasksFor(
            first, /*pathIndex=*/0, firstDeviationIndex, accepted, reverseDistances, taskHeap);

        while (accepted.size() < k) {
            // Materialize tasks while their LB might beat the best candidate.
            while (!taskHeap.isEmpty()
                && (candHeap.isEmpty() || taskHeap.peek().lowerBound < candHeap.peek().cost
                    || approxEq(taskHeap.peek().lowerBound, candHeap.peek().cost)))
            {
                SpurTask t = taskHeap.poll();
                stats.spurTasksMaterialized++;
                Candidate c = materialize(t, accepted, reverseDistances);
                if (c == null) {
                    stats.unreachableSpurs++;
                    continue;
                }
                if (seenCandidates.add(c.path.getVertexList())) {
                    candHeap.offer(c);
                    stats.candidateHeapPushes++;
                    stats.materializedCandidates++;
                }
            }
            if (candHeap.isEmpty()) {
                break;
            }
            Candidate next = candHeap.poll();
            stats.candidateHeapPops++;

            // Lower-bound certificate: if any deferred task has lowerBound < next.cost, we cannot
            // safely output next — we must continue materializing. The outer while loop already
            // ensured this, but we double-check after the pop because materialization above may
            // have produced a strictly cheaper candidate.
            if (!taskHeap.isEmpty() && taskHeap.peek().lowerBound < next.cost
                && !approxEq(taskHeap.peek().lowerBound, next.cost))
            {
                // Re-queue and loop. Should be rare in practice because we already drained tasks.
                candHeap.offer(next);
                stats.candidateHeapPushes++;
                continue;
            }
            accepted.add(next.path);
            firstDeviationIndex.add(next.firstDeviationIndex);
            int newIdx = accepted.size() - 1;
            addSpurTasksFor(
                next.path, newIdx, firstDeviationIndex, accepted, reverseDistances, taskHeap);
        }

        stats.shortestPathCalls = engine.pathQueries();
        stats.expandedVertices = engine.expandedVertices();
        stats.deferredTasksLeft = taskHeap.size();
        return accepted;
    }

    /** Run a single spur query through the engine and stitch the candidate path. */
    private Candidate runSpur(
        GraphPath<V, E> parent, int spurIndex, Set<V> bannedVertices, Set<E> bannedEdges,
        double rootCost, Map<V, Double> reverseDistances)
    {
        List<V> parentVertices = parent.getVertexList();
        List<E> parentEdges = parent.getEdgeList();
        V spurNode = parentVertices.get(spurIndex);
        V sink = parent.getEndVertex();
        V source = parent.getStartVertex();
        GraphPath<V, E> spur = engine.findPath(
            graph, spurNode, sink, bannedVertices, bannedEdges, reverseDistances);
        if (spur == null) {
            return null;
        }
        List<V> candVerts = new ArrayList<>(spurIndex + spur.getVertexList().size());
        List<E> candEdges = new ArrayList<>(spurIndex + spur.getEdgeList().size());
        for (int i = 0; i < spurIndex; i++) {
            candVerts.add(parentVertices.get(i));
            candEdges.add(parentEdges.get(i));
        }
        candVerts.addAll(spur.getVertexList());
        candEdges.addAll(spur.getEdgeList());
        double cost = rootCost + spur.getWeight();
        GraphPath<V, E> candidate = new GraphWalk<>(graph, source, sink, candVerts, candEdges, cost);
        return new Candidate(candidate, cost, spurIndex, stats.candidateOrdinal++);
    }

    /**
     * Compute the set of edges that standard Yen would ban at {@code spurIndex} given the
     * current {@code accepted} list. An edge is banned iff there exists a previously accepted
     * path whose first {@code spurIndex+1} vertices match {@code parentVertices} and whose
     * {@code spurIndex}-th edge would otherwise be the next edge of the spur.
     */
    private Set<E> computeYenBannedEdges(
        List<V> parentVertices, int spurIndex, List<GraphPath<V, E>> accepted)
    {
        Set<E> bannedEdges = new HashSet<>();
        for (GraphPath<V, E> other : accepted) {
            List<V> ov = other.getVertexList();
            if (ov.size() <= spurIndex + 1) {
                continue;
            }
            boolean prefixMatch = true;
            for (int i = 0; i <= spurIndex; i++) {
                if (!parentVertices.get(i).equals(ov.get(i))) {
                    prefixMatch = false;
                    break;
                }
            }
            if (prefixMatch) {
                bannedEdges.add(other.getEdgeList().get(spurIndex));
            }
        }
        return bannedEdges;
    }

    /**
     * True iff the spur node has at least one outgoing edge that survives the current bans
     * (banned-vertex set + banned-edge set). When this is false the spur is provably impossible
     * and the task can be skipped exactly — adding more bans later can only remove legal edges,
     * never add them, so a "false" verdict here is stable through the rest of the run.
     */
    private boolean hasLegalExit(V spurNode, Set<V> bannedVertices, Set<E> bannedEdges)
    {
        for (E e : graph.outgoingEdgesOf(spurNode)) {
            if (bannedEdges.contains(e)) {
                continue;
            }
            V v = oppositeOf(graph, spurNode, e);
            if (bannedVertices.contains(v)) {
                continue;
            }
            return true;
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------
    // Reverse distances
    // --------------------------------------------------------------------------------------------

    private Map<V, Double> computeReverseDistances(V sink)
    {
        Graph<V, E> reversed = new EdgeReversedGraph<>(graph);
        // Plain Dijkstra single-source on the reversed graph.
        Map<V, Double> dist = new HashMap<>();
        // Comparator-based PQ, no inner-generic-class headaches: entries are (vertex, dist, ord).
        PriorityQueue<Object[]> pq = new PriorityQueue<>(
            (a, b) -> {
                int c = Double.compare((Double) a[1], (Double) b[1]);
                if (c != 0) {
                    return c;
                }
                return Long.compare((Long) a[2], (Long) b[2]);
            });
        dist.put(sink, 0.0);
        long ord = 0;
        pq.offer(new Object[] { sink, 0.0, ord });
        while (!pq.isEmpty()) {
            Object[] entry = pq.poll();
            @SuppressWarnings("unchecked")
            V u = (V) entry[0];
            double du = (Double) entry[1];
            Double known = dist.get(u);
            if (known == null || du > known) {
                continue;
            }
            for (E ed : reversed.outgoingEdgesOf(u)) {
                V to = oppositeOf(reversed, u, ed);
                double w = reversed.getEdgeWeight(ed);
                if (w < 0.0) {
                    throw new IllegalArgumentException(
                        "BoundedPrunedYen requires non-negative edge weights");
                }
                double nd = du + w;
                Double existing = dist.get(to);
                if (existing == null || nd < existing) {
                    dist.put(to, nd);
                    pq.offer(new Object[] { to, nd, ++ord });
                }
            }
        }
        return dist;
    }

    private V oppositeOf(Graph<V, E> g, V u, E e)
    {
        V s = g.getEdgeSource(e);
        V t = g.getEdgeTarget(e);
        return u.equals(s) ? t : s;
    }

    // --------------------------------------------------------------------------------------------
    // Spur task creation and materialization
    // --------------------------------------------------------------------------------------------

    private void addSpurTasksFor(
        GraphPath<V, E> path, int pathIndex, List<Integer> firstDeviationIndex,
        List<GraphPath<V, E>> accepted, Map<V, Double> reverseDistances,
        PriorityQueue<SpurTask> taskHeap)
    {
        List<V> vertices = path.getVertexList();
        List<E> edges = path.getEdgeList();
        int n = vertices.size();
        if (n < 2) {
            return;
        }
        int devIndex = firstDeviationIndex.get(pathIndex);
        // Spur indices: from devIndex to n-2 (spur node is vertices[i], i is also rootPrefixLen)
        Set<V> bannedVerticesPrefix = new LinkedHashSet<>();
        double rootCost = 0.0;
        // Walk through prefix accumulating bans.
        for (int i = 0; i <= n - 2; i++) {
            if (i >= devIndex) {
                V spurNode = vertices.get(i);
                Double rev = reverseDistances.get(spurNode);
                if (rev == null || Double.isInfinite(rev)) {
                    // Unreachable from spur to sink even on the original graph -> never produces
                    // a path under any bans.
                    stats.unreachableSpurs++;
                } else {
                    // Exact impossible-spur skip. Compute the Yen banned edges for this spur and
                    // check whether the spur node has any surviving outgoing edge. Adding bans
                    // later can only remove legal edges, never add them, so a "no legal exit"
                    // verdict here is stable for the rest of the run. This eliminates the
                    // path-chain pathology where every spur's only outgoing edge gets banned.
                    Set<E> bannedEdges = computeYenBannedEdges(vertices, i, accepted);
                    if (!hasLegalExit(spurNode, bannedVerticesPrefix, bannedEdges)) {
                        stats.skippedImpossibleSpurTasks++;
                    } else {
                        double lb = rootCost + rev;
                        SpurTask t = new SpurTask(
                            pathIndex, i, new HashSet<>(bannedVerticesPrefix), rootCost, lb,
                            stats.spurTasksCreated++);
                        taskHeap.offer(t);
                    }
                }
            }
            // Update prefix bans for next iteration: ban vertex[i] going forward (it becomes part
            // of the root prefix for spur index i+1 onward).
            bannedVerticesPrefix.add(vertices.get(i));
            rootCost += graph.getEdgeWeight(edges.get(i));
        }
    }

    private Candidate materialize(
        SpurTask task, List<GraphPath<V, E>> accepted, Map<V, Double> reverseDistances)
    {
        GraphPath<V, E> parent = accepted.get(task.parentIndex);
        Set<E> bannedEdges =
            computeYenBannedEdges(parent.getVertexList(), task.spurIndex, accepted);
        return runSpur(
            parent, task.spurIndex, task.bannedVertices, bannedEdges, task.rootCost,
            reverseDistances);
    }

    private static boolean approxEq(double a, double b)
    {
        return Math.abs(a - b) <= 1e-12 * Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
    }

    // --------------------------------------------------------------------------------------------
    // Stats
    // --------------------------------------------------------------------------------------------

    /** Run statistics. Reset on every call to {@link #getPaths}. */
    public static final class Stats
    {
        public long shortestPathCalls;
        public long spurTasksCreated;
        public long spurTasksMaterialized;
        public long materializedCandidates;
        public long deferredTasksLeft;
        public long candidateHeapPushes;
        public long candidateHeapPops;
        public long unreachableSpurs;
        public long expandedVertices;
        /**
         * Number of spur indices that were skipped exactly because the spur node had no legal
         * outgoing edge after applying the prefix-vertex bans and the Yen banned-edge rule.
         * Adding more bans later can only remove legal exits, so the skip is sound.
         */
        public long skippedImpossibleSpurTasks;
        long candidateOrdinal;

        void reset()
        {
            shortestPathCalls = 0;
            spurTasksCreated = 0;
            spurTasksMaterialized = 0;
            materializedCandidates = 0;
            deferredTasksLeft = 0;
            candidateHeapPushes = 0;
            candidateHeapPops = 0;
            unreachableSpurs = 0;
            expandedVertices = 0;
            skippedImpossibleSpurTasks = 0;
            candidateOrdinal = 0;
        }

        @Override
        public String toString()
        {
            return "Stats{" + "spCalls=" + shortestPathCalls + ", tasksCreated=" + spurTasksCreated
                + ", tasksMaterialized=" + spurTasksMaterialized + ", candPushes="
                + candidateHeapPushes + ", candPops=" + candidateHeapPops + ", unreachableSpurs="
                + unreachableSpurs + ", skippedImpossibleSpurs=" + skippedImpossibleSpurTasks
                + ", deferredLeft=" + deferredTasksLeft + ", expanded=" + expandedVertices + '}';
        }
    }

    public Stats getStats()
    {
        return stats;
    }

    public String engineName()
    {
        return engine.name();
    }

    // --------------------------------------------------------------------------------------------
    // Internal helper types
    // --------------------------------------------------------------------------------------------

    private final class SpurTask
        implements Comparable<SpurTask>
    {
        final int parentIndex;
        final int spurIndex;
        final Set<V> bannedVertices;
        final double rootCost;
        final double lowerBound;
        final long ord;

        SpurTask(
            int parentIndex, int spurIndex, Set<V> bannedVertices, double rootCost,
            double lowerBound, long ord)
        {
            this.parentIndex = parentIndex;
            this.spurIndex = spurIndex;
            this.bannedVertices = bannedVertices;
            this.rootCost = rootCost;
            this.lowerBound = lowerBound;
            this.ord = ord;
        }

        @Override
        public int compareTo(SpurTask o)
        {
            int c = Double.compare(this.lowerBound, o.lowerBound);
            if (c != 0) {
                return c;
            }
            return Long.compare(this.ord, o.ord);
        }
    }

    private final class Candidate
        implements Comparable<Candidate>
    {
        final GraphPath<V, E> path;
        final double cost;
        final int firstDeviationIndex;
        final long ord;

        Candidate(GraphPath<V, E> path, double cost, int firstDeviationIndex, long ord)
        {
            this.path = path;
            this.cost = cost;
            this.firstDeviationIndex = firstDeviationIndex;
            this.ord = ord;
        }

        @Override
        public int compareTo(Candidate o)
        {
            int c = Double.compare(this.cost, o.cost);
            if (c != 0) {
                return c;
            }
            return Long.compare(this.ord, o.ord);
        }
    }

}
