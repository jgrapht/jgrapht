/*
 * (C) Copyright 2019-2026, by Semen Chudakov and Contributors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link DijkstraManyToManyShortestPaths}.
 *
 * @author Semen Chudakov
 */
public class DijkstraManyToManyShortestPathsTest extends BaseManyToManyShortestPathsTest
{

    @Test
    public void testEmptyGraph()
    {
        super.testEmptyGraph();
    }

    @Test
    public void testSourcesIsNull()
    {
        assertThrows(NullPointerException.class, () -> super.testSourcesIsNull());
    }

    @Test
    public void testTargetsIsNull()
    {
        assertThrows(NullPointerException.class, () -> super.testTargetsIsNull());
    }

    @Test
    public void testNoPath()
    {
        super.testNoPath();
    }

    @Test
    public void testNoPathMultiset()
    {
        super.testNoPathMultiSet();
    }

    @Test
    public void testDifferentSourcesAndTargetsSimpleGraph()
    {
        super.testDifferentSourcesAndTargetsSimpleGraph();
    }

    @Test
    public void testDifferentSourcesAndTargetsMultigraph()
    {
        super.testDifferentSourcesAndTargetsMultigraph();
    }

    @Test
    public void testSourcesEqualTargetsSimpleGraph()
    {
        super.testSourcesEqualTargetsSimpleGraph();
    }

    @Test
    public void testSourcesEqualTargetsMultigraph()
    {
        super.testSourcesEqualTargetsMultigraph();
    }

    @Test
    public void testOnRandomGraphs()
    {
        super.testOnRandomGraphs(100, 20, new int[][] { { 50, 30 }, { 40, 40 }, { 30, 50 } }, 50);
    }

    @Test
    public void testGetPathsSingleSourceMatchesDijkstra()
    {
        // The inherited getPaths(V) was previously implemented by issuing one
        // getPath(source, v) per vertex of the graph, which re-ran Dijkstra |V| times from the
        // same source. The optimized implementation runs a single shortest-paths search from
        // source instead. This test pins that result against a fresh DijkstraShortestPath as the
        // ground-truth oracle, and additionally verifies the unreachable-vertex contract.

        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.setEdgeWeight(graph.addEdge(1, 2), 1.5);
        graph.setEdgeWeight(graph.addEdge(2, 3), 2.0);
        graph.setEdgeWeight(graph.addEdge(1, 3), 5.0);
        graph.setEdgeWeight(graph.addEdge(3, 4), 1.0);
        // vertex 5 is intentionally isolated: unreachable from 1 in the directed graph

        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);
        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);

        // Source, reachable targets, and unreachable target (5) all covered by the oracle
        // helper — assertCorrectPaths handles the null-path / +Inf-weight contract for
        // unreachable vertices.
        assertCorrectPaths(graph, paths, 1, graph.vertexSet());

        // Pin the source-vertex contract explicitly: zero-length walk.
        assertEquals(0, paths.getPath(1).getLength());
    }

    @Test
    public void testGetPathsRejectsVertexNotInGraph()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);
        assertThrows(IllegalArgumentException.class, () -> alg.getPaths(42));
    }

    @Test
    public void testGetPathsSingleVertexGraph()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(7);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);

        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(7);
        assertCorrectPaths(graph, paths, 7, graph.vertexSet());
        assertEquals(0, paths.getPath(7).getLength());
    }

    @Test
    public void testGetPathsSourceHasNoOutgoingEdges()
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3 }) {
            graph.addVertex(v);
        }
        graph.setEdgeWeight(graph.addEdge(2, 3), 1d);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);

        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);
        assertCorrectPaths(graph, paths, 1, graph.vertexSet());
    }

    @Test
    public void testGetPathsDisconnectedGraph()
    {
        // Two disjoint components: source reaches {1,2,3}, isolated component is {10,11}
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 10, 11 }) {
            graph.addVertex(v);
        }
        graph.setEdgeWeight(graph.addEdge(1, 2), 1d);
        graph.setEdgeWeight(graph.addEdge(2, 3), 1d);
        graph.setEdgeWeight(graph.addEdge(10, 11), 1d);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);

        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);
        assertCorrectPaths(graph, paths, 1, graph.vertexSet());
    }

    @Test
    public void testGetPathsCyclicGraphWithSelfLoopOnSource()
    {
        // Graph with cycles, including a self-loop on source. The optimized override
        // must still return the trivial zero-length walk for source == target rather than
        // following the self-loop. DijkstraClosestFirstIterator handles cycles correctly;
        // this test pins that behavior.
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v : new int[] { 1, 2, 3, 4 }) {
            graph.addVertex(v);
        }
        graph.setEdgeWeight(graph.addEdge(1, 1), 5d); // self-loop on source
        graph.setEdgeWeight(graph.addEdge(1, 2), 1d);
        graph.setEdgeWeight(graph.addEdge(2, 3), 1d);
        graph.setEdgeWeight(graph.addEdge(3, 2), 1d); // back-edge creating a 2↔3 cycle
        graph.setEdgeWeight(graph.addEdge(3, 4), 1d);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);

        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);
        assertCorrectPaths(graph, paths, 1, graph.vertexSet());
        // Source-to-source path is the zero-length walk, not the self-loop.
        assertEquals(0, paths.getPath(1).getLength());
    }

    @Test
    public void testGetPathsCalledTwiceReturnsConsistentResults()
    {
        // Two back-to-back calls on the same source must produce the same SingleSourcePaths
        // content. Pins against any latent stateful caching bug introduced by the override.
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph = buildSampleGraph(11L, 30);
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(graph);

        Integer source = graph.vertexSet().iterator().next();
        SingleSourcePaths<Integer, DefaultWeightedEdge> first = alg.getPaths(source);
        SingleSourcePaths<Integer, DefaultWeightedEdge> second = alg.getPaths(source);
        for (Integer target : graph.vertexSet()) {
            assertEquals(first.getWeight(target), second.getWeight(target), 1e-12);
            GraphPath<Integer, DefaultWeightedEdge> firstPath = first.getPath(target);
            GraphPath<Integer, DefaultWeightedEdge> secondPath = second.getPath(target);
            if (firstPath == null) {
                assertNull(secondPath);
            } else {
                assertEquals(firstPath.getVertexList(), secondPath.getVertexList());
            }
        }
    }

    @Test
    public void testGetPathsFuzzAgainstDijkstraOracle()
    {
        // For 10 seeded random directed weighted graphs of varying sizes and densities,
        // assert that dmm.getPaths(source) matches DijkstraShortestPath.getPaths(source) on
        // every target. Failure mode: a regression introduced into the getPaths(V) override
        // that diverges from the canonical Dijkstra answer for some graph shape we didn't
        // hand-build.
        long[] seeds = { 1L, 2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L };
        int[] vertexCounts = { 10, 20, 35, 50, 80 };
        for (long seed : seeds) {
            Random rng = new Random(seed);
            int n = vertexCounts[(int) (Math.abs(seed) % vertexCounts.length)];
            DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                buildRandomGraph(rng, n);
            DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
                new DijkstraManyToManyShortestPaths<>(graph);

            for (Integer source : graph.vertexSet()) {
                assertCorrectPaths(graph, alg.getPaths(source), source, graph.vertexSet());
            }
        }
    }

    private static DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> buildSampleGraph(
        long seed, int n)
    {
        return buildRandomGraph(new Random(seed), n);
    }

    private static DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> buildRandomGraph(
        Random rng, int n)
    {
        DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int v = 0; v < n; v++) {
            graph.addVertex(v);
        }
        // Density chosen to give a mix of reachable and unreachable target pairs for most seeds.
        double p = 0.2;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && rng.nextDouble() < p) {
                    DefaultWeightedEdge edge = graph.addEdge(i, j);
                    if (edge != null) {
                        graph.setEdgeWeight(edge, 1d + rng.nextInt(1000));
                    }
                }
            }
        }
        return graph;
    }

    @Override
    protected ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> getAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new DijkstraManyToManyShortestPaths<>(graph);
    }
}
