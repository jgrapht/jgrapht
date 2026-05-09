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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        DijkstraShortestPath<Integer, DefaultWeightedEdge> oracle =
            new DijkstraShortestPath<>(graph);
        SingleSourcePaths<Integer, DefaultWeightedEdge> oraclePaths = oracle.getPaths(1);

        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = alg.getPaths(1);
        assertEquals(graph, paths.getGraph());
        assertEquals(Integer.valueOf(1), paths.getSourceVertex());

        // Source vertex itself
        assertEquals(0d, paths.getWeight(1), 1e-12);
        assertNotNull(paths.getPath(1));
        assertEquals(0, paths.getPath(1).getLength());

        // Reachable targets
        for (Integer target : new Integer[] { 2, 3, 4 }) {
            assertEquals(oraclePaths.getWeight(target), paths.getWeight(target), 1e-12);
            assertEquals(
                oraclePaths.getPath(target).getVertexList(),
                paths.getPath(target).getVertexList());
        }

        // Unreachable target (5): SingleSourcePaths contract is null path / +Inf weight
        assertNull(paths.getPath(5));
        assertEquals(Double.POSITIVE_INFINITY, paths.getWeight(5), 0d);
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

    @Override
    protected ManyToManyShortestPathsAlgorithm<Integer, DefaultWeightedEdge> getAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new DijkstraManyToManyShortestPaths<>(graph);
    }
}
