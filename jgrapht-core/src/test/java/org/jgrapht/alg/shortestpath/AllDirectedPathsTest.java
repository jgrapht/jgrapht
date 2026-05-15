/*
 * (C) Copyright 2016-2026, by Vera-Licona Research Group and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the AllDirectedPaths algorithm.
 *
 * @author Andrew Gainer-Dewar, Google LLC
 **/

public class AllDirectedPathsTest
{
    private static final String I1 = "I1";
    private static final String I2 = "I2";
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";
    private static final String F = "F";
    private static final String O1 = "O1";
    private static final String O2 = "O2";

    @Test
    public void testSmallExampleGraph()
    {
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = vertexSet(I1, I2);
        Set<String> targets = vertexSet(O1, O2);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, null);

        assertEquals(7, allPaths.size(), "Toy network should have correct number of simple paths");
    }

    @Test
    public void testSmallExampleGraphWithPathValidator()
    {
        PathValidator<String, DefaultEdge> pathValidator =
            (partialPath, edge) -> !"B".equals(partialPath.getGraph().getEdgeTarget(edge));

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg =
            new AllDirectedPaths<>(toyGraph(), pathValidator);

        Set<String> sources = vertexSet(I1, I2);
        Set<String> targets = vertexSet(O1, O2);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, null);

        assertEquals(
            3, allPaths.size(),
            "Toy network should have correct number of simple paths using path validator");
    }

    @Test
    public void testTrivialPaths()
    {
        // Verify fix for http://github.com/jgrapht/jgrapht/issues/234.
        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = vertexSet(I1);
        Set<String> targets = vertexSet(I1, A);

        List<GraphPath<String, DefaultEdge>> allPaths =
            pathFindingAlg.getAllPaths(sources, targets, true, 1);

        assertEquals(
            2, allPaths.size(), "Toy network should have correct number of trivial simple paths");
        assertEquals(Arrays.asList(I1), allPaths.get(0).getVertexList());
        assertEquals(Arrays.asList(I1, A), allPaths.get(1).getVertexList());
    }

    @Test
    public void testLengthOnePaths()
    {
        // Verify fix for http://github.com/jgrapht/jgrapht/issues/441.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("B", "A");

        AllDirectedPaths<String, DefaultEdge> all = new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultEdge>> allPaths =
            all.getAllPaths(graph.vertexSet(), graph.vertexSet(), true, graph.edgeSet().size());

        assertEquals(3, allPaths.size());
        assertEquals(Arrays.asList("A"), allPaths.get(0).getVertexList());
        assertEquals(Arrays.asList("B"), allPaths.get(1).getVertexList());
        assertEquals(Arrays.asList("B", "A"), allPaths.get(2).getVertexList());
    }

    @Test
    public void testLengthOnePathsWithPathValidator()
    {
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("C", "A");
        graph.addEdge("C", "B");

        PathValidator<String, DefaultEdge> pathValidator =
            (partialPath, edge) -> !"B".equals(graph.getEdgeTarget(edge));
        AllDirectedPaths<String, DefaultEdge> all = new AllDirectedPaths<>(graph, pathValidator);
        List<GraphPath<String, DefaultEdge>> allPaths =
            all.getAllPaths(graph.vertexSet(), graph.vertexSet(), true, graph.edgeSet().size());

        assertEquals(4, allPaths.size());
        assertEquals(Arrays.asList("A"), allPaths.get(0).getVertexList());
        // The following is slightly counterintuitive, as one might think that the B-excluding
        // pathValidator excludes
        // this path. However, a PathValidator is designed to check additional edges, not vertices,
        // so this is correct
        // behavior. Included a comment on this in the Javadoc of the algorithm.
        assertEquals(Arrays.asList("B"), allPaths.get(1).getVertexList());
        assertEquals(Arrays.asList("C"), allPaths.get(2).getVertexList());
        assertEquals(Arrays.asList("C", "A"), allPaths.get(3).getVertexList());
    }

    @Test
    public void testPathWeights()
    {
        // Verify fix for https://github.com/jgrapht/jgrapht/issues/617.
        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.setEdgeWeight(graph.addEdge("A", "B"), 1.2);
        graph.setEdgeWeight(graph.addEdge("A", "C"), 0);
        graph.setEdgeWeight(graph.addEdge("A", "D"), -1);
        graph.setEdgeWeight(graph.addEdge("B", "C"), 2);
        graph.setEdgeWeight(graph.addEdge("B", "D"), 1);
        graph.setEdgeWeight(graph.addEdge("C", "D"), 0.5);

        AllDirectedPaths<String, DefaultWeightedEdge> all = new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultWeightedEdge>> allPaths = all.getAllPaths("A", "D", true, 2);
        allPaths.sort(Comparator.comparing(GraphPath::getWeight));

        assertEquals(
            3, allPaths.size(), "Example weighted graph has 3 paths of length no greater than 2");
        ;

        assertEquals(Arrays.asList("A", "D"), allPaths.get(0).getVertexList());
        assertEquals(-1, allPaths.get(0).getWeight(), 0);

        assertEquals(Arrays.asList("A", "C", "D"), allPaths.get(1).getVertexList());
        assertEquals(0.5, allPaths.get(1).getWeight(), 0);

        assertEquals(Arrays.asList("A", "B", "D"), allPaths.get(2).getVertexList());
        assertEquals(2.2, allPaths.get(2).getWeight(), 0);
    }

    @Test
    public void testCycleBehavior()
    {
        Graph<String, DefaultEdge> toyGraph = toyGraph();
        toyGraph.addEdge(D, A);

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph);

        Set<String> sources = vertexSet(I1, I2);
        Set<String> targets = vertexSet(O1, O2);

        List<GraphPath<String, DefaultEdge>> allPathsWithoutCycle =
            pathFindingAlg.getAllPaths(sources, targets, true, 8);

        List<GraphPath<String, DefaultEdge>> allPathsWithCycle =
            pathFindingAlg.getAllPaths(sources, targets, false, 8);

        assertEquals(
            13, allPathsWithCycle.size(),
            "Toy network with cycle should have correct number of paths with cycle");
        assertEquals(
            7, allPathsWithoutCycle.size(),
            "Toy network with cycle should have correct number of simple paths");
    }

    @Test
    public void testMustBoundIfNonSimplePaths()
    {
        // Goofy hack to test for an exception

        AllDirectedPaths<String, DefaultEdge> pathFindingAlg = new AllDirectedPaths<>(toyGraph());

        Set<String> sources = vertexSet(I1);
        Set<String> targets = vertexSet(O1);

        assertThrows(
            IllegalArgumentException.class,
            () -> pathFindingAlg.getAllPaths(sources, targets, false, null));
    }

    @Test
    public void testTargetReachableButSourceUnreachableBranchIsIgnored()
    {
        // Sandwich-prune sanity check: when forward pruning is enabled (the default), a
        // vertex that can reach the target but is not reachable from any source must not
        // appear on any returned path. With forward pruning disabled, the algorithm still
        // returns the same path set (since the unreachable branch is not actually reachable
        // forward); forward pruning only affects preprocessing cost, not the produced path
        // set.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("S");
        graph.addVertex("A");
        graph.addVertex("T");
        graph.addEdge("S", "A");
        graph.addEdge("A", "T");

        // Dead source-side branch: U and V can reach T (U -> T) but no edge from S
        // ever leads into U or V.
        graph.addVertex("U");
        graph.addVertex("V");
        graph.addEdge("U", "T");
        graph.addEdge("V", "U");

        // Default behaviour now applies forward pruning; explicitly construct a non-pruning
        // instance so the same test covers both code paths.
        AllDirectedPaths<String, DefaultEdge> prunedAlg = new AllDirectedPaths<>(graph);
        AllDirectedPaths<String, DefaultEdge> unprunedAlg = new AllDirectedPaths<>(graph);
        unprunedAlg.setForwardPruning(false);

        for (AllDirectedPaths<String, DefaultEdge> alg : Arrays.asList(prunedAlg, unprunedAlg)) {
            List<GraphPath<String, DefaultEdge>> simplePaths = alg.getAllPaths("S", "T", true, 5);
            assertEquals(1, simplePaths.size());
            assertEquals(Arrays.asList("S", "A", "T"), simplePaths.get(0).getVertexList());

            List<GraphPath<String, DefaultEdge>> nonSimplePaths = alg.getAllPaths(
                Collections.singleton("S"), Collections.singleton("T"), false, 5);
            assertEquals(1, nonSimplePaths.size());
            assertEquals(
                Arrays.asList("S", "A", "T"), nonSimplePaths.get(0).getVertexList());
        }
    }

    @Test
    public void testZeroLengthPaths()
    {
        // Verify fix for https://github.com/jgrapht/jgrapht/issues/640.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);

        graph.addVertex("a");
        graph.addVertex("b");
        graph.addEdge("a", "b");

        List<GraphPath<String, DefaultEdge>> paths = new AllDirectedPaths<>(graph)
            .getAllPaths(graph.vertexSet(), graph.vertexSet(), false, 0);

        assertFalse(paths.isEmpty(), "We should find at least some paths!");

        paths.forEach(
            path -> assertEquals(
                0, path.getLength(),
                String.format(
                    "The path %s has length %d even though we requested only paths of length 0",
                    path, path.getLength())));
    }

    @Test
    public void testNonSimpleModeAdjacentVerticesWithSelfLoops()
    {
        // Two-vertex graph 0 → 1 with self-loops on both. Non-simple paths from 0 to 1 of length
        // ≤ 3 are:
        //   0→1 (length 1)
        //   0→0→1, 0→1→1 (length 2)
        //   0→0→0→1, 0→0→1→1, 0→1→1→1 (length 3)
        // Total = 6.
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 0);
        graph.addEdge(1, 1);
        graph.addEdge(0, 1);

        List<GraphPath<Integer, DefaultEdge>> paths =
            new AllDirectedPaths<>(graph).getAllPaths(0, 1, false, 3);
        assertEquals(6, paths.size());
    }

    @Test
    public void testNonSimpleModeCycleWithoutSelfLoops()
    {
        // Triangle 0 → 1 → 2 → 0. Non-simple paths from 0 to 1 of length ≤ 4 are:
        //   0→1 (length 1)
        //   0→1→2→0→1 (length 4)
        // No length-2 or length-3 walk from 0 ends at 1.
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        List<GraphPath<Integer, DefaultEdge>> paths =
            new AllDirectedPaths<>(graph).getAllPaths(0, 1, false, 4);
        assertEquals(2, paths.size());
    }

    @Test
    public void testNonSimpleModePathValidatorStillRespected()
    {
        // The pathValidator must still be invoked when simplePathsOnly = false, even after the
        // refactor that guards the pathVertices set behind the simple-paths flag.
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addEdge(0, 0);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 2);

        // Reject any edge into vertex 1.
        PathValidator<Integer, DefaultEdge> validator =
            (partialPath, edge) -> graph.getEdgeTarget(edge) != 1;
        List<GraphPath<Integer, DefaultEdge>> paths =
            new AllDirectedPaths<>(graph, validator).getAllPaths(0, 2, false, 3);
        // Every walk from 0 to 2 must traverse 0→1 (the only path to 1) and then 1→2.
        // The validator forbids any edge into 1, so no walks should reach 2.
        assertTrue(paths.isEmpty());
    }

    @Test
    public void testNonSimpleModeMaxLengthBoundary()
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addEdge(0, 0);
        graph.addEdge(0, 1);

        AllDirectedPaths<Integer, DefaultEdge> alg = new AllDirectedPaths<>(graph);

        // maxLength=0: only the trivial zero-length walk when source == target.
        List<GraphPath<Integer, DefaultEdge>> zeroLen =
            alg.getAllPaths(Set.of(0), Set.of(0), false, 0);
        assertEquals(1, zeroLen.size());
        assertEquals(0, zeroLen.get(0).getLength());

        // maxLength=1: zero-length walk at 0, plus 0→0 self-loop, plus 0→1 direct edge.
        List<GraphPath<Integer, DefaultEdge>> oneLen =
            alg.getAllPaths(Set.of(0), Set.of(0, 1), false, 1);
        assertEquals(3, oneLen.size());
    }

    @Test
    public void testNonSimpleModeMatchesBruteForce()
    {
        // Property-style: for a few seeded random small cyclic digraphs, compare the
        // multiset of vertex-sequences returned by getAllPaths against a brute-force walk
        // enumeration. The refactor changes the per-pop inner loop and the queue impl; this
        // test fails loudly if either changes the produced walk multiset.
        long[] seeds = { 1L, 2L, 3L, 5L, 7L };
        for (long seed : seeds) {
            DefaultDirectedGraph<Integer, DefaultEdge> graph =
                buildRandomCyclicGraph(new Random(seed), 5, 0.4, 0.2);
            Integer source = 0;
            Integer target = graph.vertexSet().size() - 1;
            int maxLength = 4;

            List<List<Integer>> bruteForce =
                bruteForceWalks(graph, source, target, maxLength);
            List<GraphPath<Integer, DefaultEdge>> actual =
                new AllDirectedPaths<>(graph).getAllPaths(source, target, false, maxLength);
            List<List<Integer>> actualVertexLists = new ArrayList<>(actual.size());
            for (GraphPath<Integer, DefaultEdge> path : actual) {
                actualVertexLists.add(path.getVertexList());
            }

            assertEquals(
                bruteForce.size(), actualVertexLists.size(),
                "walk count mismatch for seed=" + seed);
            // Compare as multisets — order of enumeration is not part of the contract.
            assertEquals(
                new HashSet<>(bruteForce), new HashSet<>(actualVertexLists),
                "walk multiset mismatch for seed=" + seed);
        }
    }

    @Test
    public void testMultipleSourcesPartialGarden()
    {
        // Two sources {S1, S2}; S1 reaches T via S1→A→T, S2 is isolated. A separate
        // source-disconnected garden vertex U → T should be pruned for both sources when
        // sandwich-prune mode is enabled, and produces the same final path set when
        // disabled.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String v : new String[] { "S1", "S2", "A", "T", "U" }) {
            graph.addVertex(v);
        }
        graph.addEdge("S1", "A");
        graph.addEdge("A", "T");
        graph.addEdge("U", "T");

        for (boolean forwardPruning : new boolean[] { false, true }) {
            AllDirectedPaths<String, DefaultEdge> alg = new AllDirectedPaths<>(graph);
            alg.setForwardPruning(forwardPruning);
            List<GraphPath<String, DefaultEdge>> paths =
                alg.getAllPaths(Set.of("S1", "S2"), Set.of("T"), true, 5);
            assertEquals(1, paths.size(), "forwardPruning=" + forwardPruning);
            assertEquals(Arrays.asList("S1", "A", "T"), paths.get(0).getVertexList());
        }
    }

    @Test
    public void testUnboundedSimplePathsWithUnreachableGarden()
    {
        // simplePathsOnly=true with maxPathLength=null. Both modes must produce the same
        // final path set; the prune only affects preprocessing.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String v : new String[] { "S", "A", "T", "U", "V" }) {
            graph.addVertex(v);
        }
        graph.addEdge("S", "A");
        graph.addEdge("A", "T");
        graph.addEdge("U", "T");
        graph.addEdge("V", "U");

        for (boolean forwardPruning : new boolean[] { false, true }) {
            AllDirectedPaths<String, DefaultEdge> alg = new AllDirectedPaths<>(graph);
            alg.setForwardPruning(forwardPruning);
            List<GraphPath<String, DefaultEdge>> paths =
                alg.getAllPaths(Set.of("S"), Set.of("T"), true, null);
            assertEquals(1, paths.size(), "forwardPruning=" + forwardPruning);
            assertEquals(Arrays.asList("S", "A", "T"), paths.get(0).getVertexList());
        }
    }

    @Test
    public void testSourceEqualsTargetInDisconnectedGraph()
    {
        // S is both source and target. A second disconnected vertex U has an edge into S
        // (so S is backward-reachable from U) but U is not forward-reachable from S. The
        // trivial zero-length walk at S must still be produced under both modes.
        DefaultDirectedGraph<String, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex("S");
        graph.addVertex("U");
        graph.addEdge("U", "S");

        for (boolean forwardPruning : new boolean[] { false, true }) {
            AllDirectedPaths<String, DefaultEdge> alg = new AllDirectedPaths<>(graph);
            alg.setForwardPruning(forwardPruning);
            List<GraphPath<String, DefaultEdge>> paths =
                alg.getAllPaths(Set.of("S"), Set.of("S"), true, 5);
            assertEquals(1, paths.size(), "forwardPruning=" + forwardPruning);
            assertEquals(0, paths.get(0).getLength());
        }
    }

    @Test
    public void testDenseStronglyConnectedLossCase()
    {
        // Loss case shape: small dense strongly-connected digraph where every vertex is
        // both forward-reachable from source and backward-reachable from target. Forward
        // pruning cannot drop anything here. Tests that enabling it does not change the
        // result on this shape — bounded overhead is OK; wrong answers are not.
        int n = 6;
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        for (int v = 0; v < n; v++) {
            graph.addVertex(v);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    graph.addEdge(i, j);
                }
            }
        }
        int maxLength = 3;
        List<List<Integer>> expected = bruteForceSimplePaths(graph, 0, n - 1, maxLength);

        for (boolean forwardPruning : new boolean[] { false, true }) {
            AllDirectedPaths<Integer, DefaultEdge> alg = new AllDirectedPaths<>(graph);
            alg.setForwardPruning(forwardPruning);
            List<GraphPath<Integer, DefaultEdge>> actual =
                alg.getAllPaths(0, n - 1, true, maxLength);
            assertEquals(
                new HashSet<>(expected), vertexListsAsSet(actual),
                "forwardPruning=" + forwardPruning);
        }
    }

    @Test
    public void testFuzzAgainstBruteForce()
    {
        // For 8 seeded random small directed graphs, compare the path set returned by
        // AllDirectedPaths against a brute-force enumeration in both prune modes. The two
        // modes must always produce the same path set as each other and as the brute-force
        // oracle. DefaultDirectedGraph has no parallel edges, so set semantics (rather
        // than multiset) are sufficient to detect any divergence. Mixes:
        //   - simple vs non-simple mode
        //   - reachable source-disconnected garden vertices and partial reachability
        //   - small bounded maxLength so the brute force terminates fast
        long[] seeds = { 1L, 2L, 3L, 5L, 7L, 11L, 13L, 17L };
        for (long seed : seeds) {
            Random rng = new Random(seed);
            DefaultDirectedGraph<Integer, DefaultEdge> graph =
                buildRandomDigraphWithIsolatedPocket(rng, 4, 4, 0.4);
            Integer source = 0;
            Integer target = 3;
            int maxLength = 4;

            Set<List<Integer>> expectedSimple =
                new HashSet<>(bruteForceSimplePaths(graph, source, target, maxLength));
            Set<List<Integer>> expectedNonSimple =
                new HashSet<>(bruteForceWalks(graph, source, target, maxLength));

            for (boolean forwardPruning : new boolean[] { false, true }) {
                AllDirectedPaths<Integer, DefaultEdge> alg = new AllDirectedPaths<>(graph);
                alg.setForwardPruning(forwardPruning);
                Set<List<Integer>> actualSimple = vertexListsAsSet(
                    alg.getAllPaths(source, target, true, maxLength));
                assertEquals(
                    expectedSimple, actualSimple,
                    "simple-mode path set mismatch seed=" + seed
                        + " forwardPruning=" + forwardPruning);

                Set<List<Integer>> actualNonSimple = vertexListsAsSet(
                    alg.getAllPaths(source, target, false, maxLength));
                assertEquals(
                    expectedNonSimple, actualNonSimple,
                    "non-simple-mode path set mismatch seed=" + seed
                        + " forwardPruning=" + forwardPruning);
            }
        }
    }

    private static DefaultDirectedGraph<Integer, DefaultEdge> buildRandomCyclicGraph(
        Random rng, int n, double edgeProbability, double selfLoopProbability)
    {
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        for (int v = 0; v < n; v++) {
            graph.addVertex(v);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    if (rng.nextDouble() < selfLoopProbability) {
                        graph.addEdge(i, j);
                    }
                } else if (rng.nextDouble() < edgeProbability) {
                    graph.addEdge(i, j);
                }
            }
        }
        return graph;
    }

    private static DefaultDirectedGraph<Integer, DefaultEdge> buildRandomDigraphWithIsolatedPocket(
        Random rng, int mainSize, int pocketSize, double edgeProbability)
    {
        // mainSize vertices labeled 0..mainSize-1 form the main connected component.
        // pocketSize vertices labeled mainSize..mainSize+pocketSize-1 are a separate pocket
        // with edges into vertex (mainSize-1) but no edges from the main component to the
        // pocket — i.e. source-unreachable but target-reachable. Exercises the sandwich prune.
        DefaultDirectedGraph<Integer, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
        for (int v = 0; v < mainSize + pocketSize; v++) {
            graph.addVertex(v);
        }
        for (int i = 0; i < mainSize; i++) {
            for (int j = 0; j < mainSize; j++) {
                if (i != j && rng.nextDouble() < edgeProbability) {
                    graph.addEdge(i, j);
                }
            }
        }
        // Guarantee at least one path 0 → ... → mainSize-1 exists by adding a chain edge.
        for (int i = 0; i + 1 < mainSize; i++) {
            if (graph.getEdge(i, i + 1) == null) {
                graph.addEdge(i, i + 1);
            }
        }
        for (int p = 0; p < pocketSize; p++) {
            int pocketVertex = mainSize + p;
            graph.addEdge(pocketVertex, mainSize - 1);
            // intra-pocket edges (do not connect back to main)
            for (int q = 0; q < pocketSize; q++) {
                int otherPocketVertex = mainSize + q;
                if (p != q && rng.nextDouble() < edgeProbability) {
                    graph.addEdge(pocketVertex, otherPocketVertex);
                }
            }
        }
        return graph;
    }

    private static Set<List<Integer>> vertexListsAsSet(List<GraphPath<Integer, DefaultEdge>> paths)
    {
        Set<List<Integer>> result = new HashSet<>(paths.size());
        for (GraphPath<Integer, DefaultEdge> path : paths) {
            result.add(path.getVertexList());
        }
        return result;
    }

    private static List<List<Integer>> bruteForceSimplePaths(
        Graph<Integer, DefaultEdge> graph, Integer source, Integer target, int maxLength)
    {
        List<List<Integer>> paths = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        current.add(source);
        Set<Integer> visited = new HashSet<>();
        visited.add(source);
        bruteForceSimplePathsRec(graph, target, maxLength, current, visited, paths);
        return paths;
    }

    private static void bruteForceSimplePathsRec(
        Graph<Integer, DefaultEdge> graph, Integer target, int remaining, List<Integer> current,
        Set<Integer> visited, List<List<Integer>> paths)
    {
        Integer head = current.get(current.size() - 1);
        if (target.equals(head)) {
            paths.add(new ArrayList<>(current));
        }
        if (remaining == 0) {
            return;
        }
        for (DefaultEdge edge : graph.outgoingEdgesOf(head)) {
            Integer next = graph.getEdgeTarget(edge);
            if (visited.contains(next)) {
                continue;
            }
            current.add(next);
            visited.add(next);
            bruteForceSimplePathsRec(graph, target, remaining - 1, current, visited, paths);
            current.remove(current.size() - 1);
            visited.remove(next);
        }
    }

    private static List<List<Integer>> bruteForceWalks(
        Graph<Integer, DefaultEdge> graph, Integer source, Integer target, int maxLength)
    {
        List<List<Integer>> walks = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(source);
        bruteForceWalksRec(graph, target, maxLength, stack, walks);
        return walks;
    }

    private static void bruteForceWalksRec(
        Graph<Integer, DefaultEdge> graph, Integer target, int remaining, Deque<Integer> stack,
        List<List<Integer>> walks)
    {
        Integer current = stack.peek();
        if (target.equals(current)) {
            List<Integer> walk = new ArrayList<>(stack);
            Collections.reverse(walk);
            walks.add(walk);
        }
        if (remaining == 0) {
            return;
        }
        for (DefaultEdge edge : graph.outgoingEdgesOf(current)) {
            Integer next = graph.getEdgeTarget(edge);
            stack.push(next);
            bruteForceWalksRec(graph, target, remaining - 1, stack, walks);
            stack.pop();
        }
    }

    private static Graph<String, DefaultEdge> toyGraph()
    {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        graph.addVertex(I1);
        graph.addVertex(I2);
        graph.addVertex(A);
        graph.addVertex(B);
        graph.addVertex(C);
        graph.addVertex(D);
        graph.addVertex(E);
        graph.addVertex(F);
        graph.addVertex(O1);
        graph.addVertex(O2);

        graph.addEdge(I1, A);
        graph.addEdge(I1, B);

        graph.addEdge(I2, B);
        graph.addEdge(I2, C);

        graph.addEdge(A, B);
        graph.addEdge(A, D);
        graph.addEdge(A, E);

        graph.addEdge(B, E);

        graph.addEdge(C, B);
        graph.addEdge(C, F);

        graph.addEdge(D, E);

        graph.addEdge(E, O1);

        graph.addEdge(F, O2);

        return graph;
    }

    private static HashSet<String> vertexSet(String... vertices)
    {
        return new HashSet<>(Arrays.asList(vertices));
    }
}
