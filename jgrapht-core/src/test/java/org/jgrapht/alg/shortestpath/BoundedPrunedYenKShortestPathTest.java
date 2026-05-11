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
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests asserting that {@link BoundedPrunedYenKShortestPath} produces the same ordered
 * sequence of path weights as {@link YenKShortestPath} on a variety of graphs.
 */
public class BoundedPrunedYenKShortestPathTest
{
    private static final double EPS = 1e-9;

    @Test
    public void testNegativeKThrows()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 1, 2, 1.0);
        assertThrows(
            IllegalArgumentException.class,
            () -> new BoundedPrunedYenKShortestPath<>(g).getPaths(1, 2, -1));
    }

    @Test
    public void testKZeroReturnsEmpty()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new BoundedPrunedYenKShortestPath<>(g).getPaths(1, 2, 0);
        assertEquals(0, paths.size());
    }

    @Test
    public void testUnreachableSink()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new BoundedPrunedYenKShortestPath<>(g).getPaths(1, 2, 5);
        assertEquals(0, paths.size());
    }

    @Test
    public void testSimpleDiamondMatchesStandardYen()
    {
        // 1 -> 2 -> 4
        // 1 -> 3 -> 4
        // 1 -> 4 (longer)
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 4 }) {
            g.addVertex(v);
        }
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 4, 2.0);
        Graphs.addEdge(g, 1, 3, 2.0);
        Graphs.addEdge(g, 3, 4, 1.0);
        Graphs.addEdge(g, 1, 4, 10.0);

        assertSamePathWeights(g, 1, 4, 5);
    }

    @Test
    public void testTieEqualWeights()
    {
        // Two equal-weight paths from 1 to 4: 1-2-4 and 1-3-4.
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 4 }) {
            g.addVertex(v);
        }
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 4, 1.0);
        Graphs.addEdge(g, 1, 3, 1.0);
        Graphs.addEdge(g, 3, 4, 1.0);

        assertSamePathWeights(g, 1, 4, 4);
    }

    @Test
    public void testCyclicGraph()
    {
        // 1 -> 2 -> 3 -> 1 (cycle), 2 -> 3 path used
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3 }) {
            g.addVertex(v);
        }
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 3, 1.0);
        Graphs.addEdge(g, 3, 1, 1.0);
        Graphs.addEdge(g, 1, 3, 5.0);

        assertSamePathWeights(g, 1, 3, 5);
    }

    @Test
    public void testLayeredDagSeveralK()
    {
        Graph<Integer, DefaultWeightedEdge> g = layeredDag(4, 4, 12345L);
        for (int k : new int[] { 1, 2, 5, 10 }) {
            assertSamePathWeights(g, 0, sinkOf(g), k);
        }
    }

    @Test
    public void testRandomDag()
    {
        Graph<Integer, DefaultWeightedEdge> g = randomDag(80, 320, 99L);
        assertSamePathWeights(g, 0, 79, 20);
    }

    @Test
    public void testGridGraph()
    {
        Graph<Integer, DefaultWeightedEdge> g = grid(8, 8, 7L);
        assertSamePathWeights(g, 0, 8 * 8 - 1, 10);
    }

    @Test
    public void testAStarEngineMatchesStandardYen()
    {
        Graph<Integer, DefaultWeightedEdge> g = layeredDag(5, 5, 42L);
        BoundedPrunedYenKShortestPath<Integer, DefaultWeightedEdge> bpAStar =
            new BoundedPrunedYenKShortestPath<>(g, new AStarSpurEngine<>());
        YenKShortestPath<Integer, DefaultWeightedEdge> yen = new YenKShortestPath<>(g);
        int sink = sinkOf(g);
        List<Double> bp = weights(bpAStar.getPaths(0, sink, 15));
        List<Double> std = weights(yen.getPaths(0, sink, 15));
        assertEqualsWeights(std, bp, "AStar engine vs standard Yen");
    }

    @Test
    public void testStatsNonNegative()
    {
        Graph<Integer, DefaultWeightedEdge> g = randomDag(100, 600, 13L);
        BoundedPrunedYenKShortestPath<Integer, DefaultWeightedEdge> bp =
            new BoundedPrunedYenKShortestPath<>(g);
        bp.getPaths(0, 99, 25);
        BoundedPrunedYenKShortestPath.Stats s = bp.getStats();
        assertNotNull(s);
        assertTrue(s.shortestPathCalls > 0);
        assertTrue(s.spurTasksCreated >= 0);
        assertTrue(s.spurTasksMaterialized <= s.spurTasksCreated);
        assertTrue(s.candidateHeapPops >= 0);
    }

    @Test
    public void testNonDecreasingWeights()
    {
        Graph<Integer, DefaultWeightedEdge> g = layeredDag(6, 4, 1L);
        BoundedPrunedYenKShortestPath<Integer, DefaultWeightedEdge> bp =
            new BoundedPrunedYenKShortestPath<>(g);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths = bp.getPaths(0, sinkOf(g), 30);
        double prev = Double.NEGATIVE_INFINITY;
        for (GraphPath<Integer, DefaultWeightedEdge> p : paths) {
            assertTrue(p.getWeight() >= prev - EPS, "weights must be non-decreasing");
            prev = p.getWeight();
        }
    }

    // ---------------------------------------------------------------------------------------
    // Edge cases
    // ---------------------------------------------------------------------------------------

    @Test
    public void testKLargerThanTotalPaths()
    {
        // Diamond: exactly 3 simple paths from 1 to 4. Asking for 100 must return only 3.
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 4 }) {
            g.addVertex(v);
        }
        Graphs.addEdge(g, 1, 2, 1.0);
        Graphs.addEdge(g, 2, 4, 1.0);
        Graphs.addEdge(g, 1, 3, 2.0);
        Graphs.addEdge(g, 3, 4, 2.0);
        Graphs.addEdge(g, 1, 4, 10.0);
        // Match YenKShortestPath behaviour exactly — both should terminate and return all paths.
        assertSamePathWeights(g, 1, 4, 100);
    }

    @Test
    public void testZeroWeightEdges()
    {
        // Zero weights are allowed (non-negative). Multiple zero-weight paths must all be found.
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 4 }) {
            g.addVertex(v);
        }
        Graphs.addEdge(g, 1, 2, 0.0);
        Graphs.addEdge(g, 2, 4, 0.0);
        Graphs.addEdge(g, 1, 3, 0.0);
        Graphs.addEdge(g, 3, 4, 0.0);
        Graphs.addEdge(g, 1, 4, 1.0);
        assertSamePathWeights(g, 1, 4, 5);
    }

    @Test
    public void testSingleEdgeGraph()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        Graphs.addEdge(g, 1, 2, 7.5);
        assertSamePathWeights(g, 1, 2, 5);
    }

    @Test
    public void testSourceEqualsSink()
    {
        // Match YenKShortestPath behaviour for source==sink (whatever it returns).
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        Graphs.addEdge(g, 1, 2, 1.0);
        assertSamePathWeights(g, 1, 1, 3);
    }

    @Test
    public void testLargeKDenseDag()
    {
        // Dense small DAG; ask for many paths. Stress-tests the dedup and termination logic
        // when the candidate heap empties before reaching k.
        Graph<Integer, DefaultWeightedEdge> g = layeredDag(4, 5, 314L);
        assertSamePathWeights(g, 0, sinkOf(g), 500);
    }

    @Test
    public void testRejectsNegativeWeights()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(g, 1, 2, -1.0);
        BoundedPrunedYenKShortestPath<Integer, DefaultWeightedEdge> bp =
            new BoundedPrunedYenKShortestPath<>(g);
        assertThrows(IllegalArgumentException.class, () -> bp.getPaths(1, 2, 1));
    }

    // ---------------------------------------------------------------------------------------
    // Both engines: explicit cross-check
    // ---------------------------------------------------------------------------------------

    @Test
    public void testBothEnginesAgreeWithStandardYen()
    {
        long[] seeds = { 1L, 2L, 3L, 4L, 5L };
        for (long seed : seeds) {
            Graph<Integer, DefaultWeightedEdge> g = layeredDag(5, 4, seed);
            int sink = sinkOf(g);
            List<Double> std = weights(new YenKShortestPath<>(g).getPaths(0, sink, 12));
            List<Double> bpDij = weights(
                new BoundedPrunedYenKShortestPath<>(g, new DijkstraSpurEngine<>())
                    .getPaths(0, sink, 12));
            List<Double> bpAstar = weights(
                new BoundedPrunedYenKShortestPath<>(g, new AStarSpurEngine<>())
                    .getPaths(0, sink, 12));
            assertEqualsWeights(std, bpDij, "Dijkstra engine seed=" + seed);
            assertEqualsWeights(std, bpAstar, "AStar engine seed=" + seed);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Fuzz tests against YenKShortestPath
    // ---------------------------------------------------------------------------------------

    /**
     * Property-style fuzz test: generate a wide population of random DAGs with varied size and
     * density, run both engines, and assert each returns the same ordered weight sequence as
     * {@link YenKShortestPath}. Any divergence aborts with the failing case so it is reproducible
     * from the seed alone.
     */
    @Test
    public void testFuzzRandomDagsAgainstStandardYen()
    {
        Random meta = new Random(20260509L);
        int totalCases = 40;
        for (int caseIdx = 0; caseIdx < totalCases; caseIdx++) {
            int n = 8 + meta.nextInt(80); // 8..87 vertices
            int extraEdges = meta.nextInt(2 * n);
            int k = 1 + meta.nextInt(25);
            long seed = meta.nextLong();
            Graph<Integer, DefaultWeightedEdge> g =
                randomDagWithChain(n, extraEdges, seed);
            String label = String.format(
                "case=%d n=%d extras=%d k=%d seed=%d", caseIdx, n, extraEdges, k, seed);
            assertSameForBothEngines(g, 0, n - 1, k, label);
        }
    }

    /**
     * Fuzz test with cycles: random sparse graphs that may contain cycles. Yen's algorithm
     * returns loopless paths regardless. Asserts both engines match {@link YenKShortestPath} on
     * each generated graph.
     */
    @Test
    public void testFuzzRandomCyclicGraphsAgainstStandardYen()
    {
        Random meta = new Random(987654321L);
        int totalCases = 30;
        for (int caseIdx = 0; caseIdx < totalCases; caseIdx++) {
            int n = 6 + meta.nextInt(40);
            int m = n + meta.nextInt(2 * n);
            int k = 1 + meta.nextInt(20);
            long seed = meta.nextLong();
            Graph<Integer, DefaultWeightedEdge> g = randomDigraph(n, m, seed);
            int sink = n - 1;
            if (!g.containsVertex(0) || !g.containsVertex(sink)) {
                continue; // skip degenerate cases
            }
            String label = String.format(
                "cyclic case=%d n=%d m=%d k=%d seed=%d", caseIdx, n, m, k, seed);
            assertSameForBothEngines(g, 0, sink, k, label);
        }
    }

    @Test
    public void testFuzzGridsAndChainsAgainstStandardYen()
    {
        long[] seeds = { 11L, 22L, 33L, 44L, 55L };
        // grids of varied shapes
        for (long seed : seeds) {
            for (int rows : new int[] { 3, 5, 8 }) {
                for (int cols : new int[] { 3, 5, 8 }) {
                    Graph<Integer, DefaultWeightedEdge> g = grid(rows, cols, seed);
                    String label = String.format(
                        "grid %dx%d seed=%d", rows, cols, seed);
                    assertSameForBothEngines(g, 0, rows * cols - 1, 8, label);
                }
            }
        }
    }

    /** Helper used by fuzz tests: assert both engines match standard Yen on the given graph. */
    private static void assertSameForBothEngines(
        Graph<Integer, DefaultWeightedEdge> g, Integer source, Integer sink, int k, String label)
    {
        List<Double> std = weights(new YenKShortestPath<>(g).getPaths(source, sink, k));
        List<Double> bpDij = weights(
            new BoundedPrunedYenKShortestPath<>(g, new DijkstraSpurEngine<>())
                .getPaths(source, sink, k));
        List<Double> bpAstar = weights(
            new BoundedPrunedYenKShortestPath<>(g, new AStarSpurEngine<>())
                .getPaths(source, sink, k));
        assertEqualsWeights(std, bpDij, label + " [Dijkstra]");
        assertEqualsWeights(std, bpAstar, label + " [AStar]");
    }

    // ---------------------------------------------------------------------------------------
    // Impossible-spur skip
    // ---------------------------------------------------------------------------------------

    @Test
    public void testImpossibleSpurSkipEliminatesPathChainTasks()
    {
        // On a pure path chain, every spur's only outgoing edge is banned by the Yen rule
        // (because the only accepted path uses it). The exact impossible-spur skip must catch
        // every one of these and create zero spur tasks. Total skipped should equal n-1.
        int n = 100;
        Graph<Integer, DefaultWeightedEdge> g = pathChain(n, 11L);
        BoundedPrunedYenKShortestPath<Integer, DefaultWeightedEdge> bp =
            new BoundedPrunedYenKShortestPath<>(g, new DijkstraSpurEngine<>());
        bp.getPaths(0, n - 1, 5);
        BoundedPrunedYenKShortestPath.Stats s = bp.getStats();
        assertEquals(0L, s.spurTasksCreated, "no doomed tasks should be created on a path chain");
        assertEquals(
            (long) (n - 1), s.skippedImpossibleSpurTasks,
            "every spur position must be skipped on a path chain");
    }

    @Test
    public void testPathChainReturnsSinglePath()
    {
        // Single-path DAG, k > 1: must return just that one path.
        Graph<Integer, DefaultWeightedEdge> g = pathChain(20, 99L);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new BoundedPrunedYenKShortestPath<>(g, new DijkstraSpurEngine<>())
                .getPaths(0, 19, 5);
        assertEquals(1, paths.size());
    }

    /** Simple linear chain 0 -&gt; 1 -&gt; ... -&gt; n-1; helper used by the new tests above. */
    static Graph<Integer, DefaultWeightedEdge> pathChain(int n, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i + 1 < n; i++) {
            Graphs.addEdge(g, i, i + 1, 1.0 + rnd.nextInt(5));
        }
        return g;
    }

    // ---------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------

    private static <V, E> void assertSamePathWeights(Graph<V, E> g, V src, V sink, int k)
    {
        List<Double> std = weights(new YenKShortestPath<>(g).getPaths(src, sink, k));
        List<Double> bp =
            weights(new BoundedPrunedYenKShortestPath<>(g).getPaths(src, sink, k));
        assertEqualsWeights(std, bp, "standard vs bounded-pruned");
    }

    private static void assertEqualsWeights(List<Double> std, List<Double> bp, String label)
    {
        assertEquals(std.size(), bp.size(), label + ": path count");
        for (int i = 0; i < std.size(); i++) {
            assertEquals(std.get(i), bp.get(i), EPS, label + ": weight at index " + i);
        }
    }

    private static <V, E> List<Double> weights(List<GraphPath<V, E>> paths)
    {
        List<Double> w = new ArrayList<>(paths.size());
        for (GraphPath<V, E> p : paths) {
            w.add(p.getWeight());
        }
        return w;
    }

    private static int sinkOf(Graph<Integer, ?> g)
    {
        int max = 0;
        for (Integer v : g.vertexSet()) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /** Layered DAG with `layers` layers of `width` vertices, plus source 0 and sink (last). */
    static Graph<Integer, DefaultWeightedEdge> layeredDag(int layers, int width, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int src = 0;
        g.addVertex(src);
        int next = 1;
        int[][] layerVerts = new int[layers][width];
        for (int l = 0; l < layers; l++) {
            for (int j = 0; j < width; j++) {
                layerVerts[l][j] = next;
                g.addVertex(next++);
            }
        }
        int sink = next;
        g.addVertex(sink);
        // src -> first layer
        for (int j = 0; j < width; j++) {
            Graphs.addEdge(g, src, layerVerts[0][j], 1.0 + rnd.nextInt(5));
        }
        // between layers
        for (int l = 0; l + 1 < layers; l++) {
            for (int j = 0; j < width; j++) {
                for (int j2 = 0; j2 < width; j2++) {
                    if (rnd.nextDouble() < 0.5) {
                        Graphs.addEdge(g, layerVerts[l][j], layerVerts[l + 1][j2],
                            1.0 + rnd.nextInt(5));
                    }
                }
            }
        }
        // last layer -> sink
        for (int j = 0; j < width; j++) {
            Graphs.addEdge(g, layerVerts[layers - 1][j], sink, 1.0 + rnd.nextInt(5));
        }
        return g;
    }

    /**
     * DAG with a guaranteed source-to-sink chain plus {@code extraEdges} random forward edges.
     * Used by the fuzz tests so source 0 and sink n-1 are always connected.
     */
    static Graph<Integer, DefaultWeightedEdge> randomDagWithChain(int n, int extraEdges, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i + 1 < n; i++) {
            Graphs.addEdge(g, i, i + 1, 1.0 + rnd.nextInt(5));
        }
        int placed = 0;
        int attempts = 0;
        while (placed < extraEdges && attempts < extraEdges * 10 + 50) {
            attempts++;
            int u = rnd.nextInt(n - 1);
            int v = u + 1 + rnd.nextInt(n - 1 - u);
            if (g.containsEdge(u, v)) {
                continue;
            }
            Graphs.addEdge(g, u, v, 1.0 + rnd.nextInt(10));
            placed++;
        }
        return g;
    }

    /**
     * Random simple digraph on n vertices that may contain cycles. May leave 0 or n-1 unreachable
     * from each other; callers should handle that. Edge weights in [1, 10).
     */
    static Graph<Integer, DefaultWeightedEdge> randomDigraph(int n, int m, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        int placed = 0;
        int attempts = 0;
        while (placed < m && attempts < m * 10 + 50) {
            attempts++;
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v || g.containsEdge(u, v)) {
                continue;
            }
            Graphs.addEdge(g, u, v, 1.0 + rnd.nextInt(9));
            placed++;
        }
        return g;
    }

    /** Random DAG on n vertices 0..n-1 with edges only (u,v) where u&lt;v. */
    static Graph<Integer, DefaultWeightedEdge> randomDag(int n, int m, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        // chain to ensure source-to-sink reachability
        for (int i = 0; i + 1 < n; i++) {
            Graphs.addEdge(g, i, i + 1, 1.0 + rnd.nextInt(5));
        }
        int extra = Math.max(0, m - (n - 1));
        for (int e = 0; e < extra; e++) {
            int u = rnd.nextInt(n - 1);
            int v = u + 1 + rnd.nextInt(n - 1 - u);
            if (g.containsEdge(u, v)) {
                continue;
            }
            Graphs.addEdge(g, u, v, 1.0 + rnd.nextInt(10));
        }
        return g;
    }

    static Graph<Integer, DefaultWeightedEdge> grid(int rows, int cols, long seed)
    {
        Random rnd = new Random(seed);
        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        int n = rows * cols;
        for (int i = 0; i < n; i++) {
            g.addVertex(i);
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int u = r * cols + c;
                if (c + 1 < cols) {
                    Graphs.addEdge(g, u, r * cols + c + 1, 1.0 + rnd.nextInt(3));
                }
                if (r + 1 < rows) {
                    Graphs.addEdge(g, u, (r + 1) * cols + c, 1.0 + rnd.nextInt(3));
                }
            }
        }
        return g;
    }
}
