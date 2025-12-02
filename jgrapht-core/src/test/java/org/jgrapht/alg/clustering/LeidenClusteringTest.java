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
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests
 *
 * @author xiangyu MAO and Hanine Gharsalli
 */
public class LeidenClusteringTest {

    /* --------------------------------------------------------------
     *  Utility helper for running Leiden
     * -------------------------------------------------------------- */
    private <V> LeidenClustering<V, DefaultWeightedEdge> run(Graph<V, DefaultWeightedEdge> g) {
        return new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
    }

    /* --------------------------------------------------------------
     *  TEST 1 — Graph with isolated vertices
     * -------------------------------------------------------------- */
    @Test
    public void testSingletonEdges() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        g.addVertex(2);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(2, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRefinementOnTrianglePlusIsolated() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 4; i++) g.addVertex(i);

        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        List<Set<Integer>> clusters = lc.getClustering().getClusters();

        assertEquals(2, clusters.size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testEmptyGraph() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(0, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testStarGraph() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 0; i < 6; i++) g.addVertex(i);
        for (int i = 1; i < 6; i++) g.addEdge(0, i);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertTrue(lc.getClustering().getClusters().size() >= 1);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testSingleVertex() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);

        assertEquals(1, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testSelfLoops() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        DefaultWeightedEdge e = g.addEdge(1, 1);
        g.setEdgeWeight(e, 5.0);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(1, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testCompleteGraphSingleCommunity() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 0; i < 5; i++) g.addVertex(i);
        for (int i = 0; i < 5; i++)
            for (int j = i + 1; j < 5; j++)
                g.addEdge(i, j);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(1, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testLeidenFixesLouvainDisconnectedCommunity() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Community A
        g.addVertex(1);
        g.addVertex(2);
        g.addEdge(1, 2);

        // Community B — but disconnected from itself
        g.addVertex(3);
        g.addVertex(4);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(3, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testBridgeRemovalCreatesCommunities() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 4; i++) g.addVertex(i);

        g.addEdge(1, 2);
        g.addEdge(3, 4);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertTrue(lc.getClustering().getClusters().size() >= 2);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testTwoBlocks() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Two cycles of 4 nodes
        int[][] blocks = {{1, 2, 3, 4}, {5, 6, 7, 8}};

        for (int[] block : blocks) {
            for (int v : block) g.addVertex(v);
            for (int i = 0; i < block.length; i++)
                g.addEdge(block[i], block[(i + 1) % block.length]);
        }

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        int size = lc.getClustering().getClusters().size();

        assertTrue(size >= 2 && size <= 4);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testTrianglePairs() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        int[][] triangles = {{1, 2, 3}, {4, 5, 6}};

        for (int[] tri : triangles) {
            for (int v : tri) g.addVertex(v);
            g.addEdge(tri[0], tri[1]);
            g.addEdge(tri[1], tri[2]);
            g.addEdge(tri[2], tri[0]);
        }

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        int size = lc.getClustering().getClusters().size();

        assertTrue(size >= 2 && size <= 3);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testDisconnectedGraphManyComponents() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 10; i++) g.addVertex(i);
        for (int i = 1; i <= 10; i += 2) g.addEdge(i, i + 1);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertTrue(lc.getClustering().getClusters().size() >= 5);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRandomGraphSmall() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        Random r = new Random(42);

        for (int i = 0; i < 12; i++) g.addVertex(i);

        for (int i = 0; i < 12; i++)
            for (int j = i + 1; j < 12; j++)
                if (r.nextDouble() < 0.15) g.addEdge(i, j);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertTrue(lc.getClustering().getClusters().size() >= 1);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testWeightedTwoCommunities() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Community 1
        g.addVertex(1);
        g.addVertex(2);
        DefaultWeightedEdge e = g.addEdge(1, 2);
        g.setEdgeWeight(e, 5.0);

        // Community 2
        g.addVertex(3);
        g.addVertex(4);
        DefaultWeightedEdge f = g.addEdge(3, 4);
        g.setEdgeWeight(f, 5.0);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);

        int size = lc.getClustering().getClusters().size();
        assertTrue(size >= 2 && size <= 4);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testCliquePlusTail() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 5; i++) g.addVertex(i);
        for (int i = 1; i <= 5; i++)
            for (int j = i + 1; j <= 5; j++)
                g.addEdge(i, j);

        g.addVertex(6);
        g.addEdge(5, 6);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertTrue(lc.getClustering().getClusters().size() >= 1);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testWeightedCPM() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 6; i++) g.addVertex(i);

        DefaultWeightedEdge e1 = g.addEdge(1, 2);
        g.setEdgeWeight(e1, 3.0);
        DefaultWeightedEdge e2 = g.addEdge(2, 3);
        g.setEdgeWeight(e2, 2.0);

        DefaultWeightedEdge e3 = g.addEdge(4, 5);
        g.setEdgeWeight(e3, 3.0);
        DefaultWeightedEdge e4 = g.addEdge(5, 6);
        g.setEdgeWeight(e4, 2.0);

        LeidenClustering<Integer, DefaultWeightedEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.CPM);

        assertTrue(lc.getClustering().getClusters().size() >= 2);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testNoEdges() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 5; i++) g.addVertex(i);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        assertEquals(5, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testCompleteGraphPlusTail() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 1; i <= 5; i++) g.addVertex(i);
        for (int i = 1; i <= 5; i++)
            for (int j = i + 1; j <= 5; j++)
                g.addEdge(i, j);

        g.addVertex(6);
        g.addEdge(6, 5);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);

        assertTrue(lc.getClustering().getClusters().size() >= 1);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRefinementSplitsDisconnectedCommunity() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addEdge(1, 2);

        g.addVertex(3);
        g.addVertex(4);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);

        assertEquals(3, lc.getClustering().getClusters().size());
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testCachingReusesResult() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addEdge(1, 2);

        LeidenClustering<Integer, DefaultWeightedEdge> lc = run(g);
        Clustering<Integer> c1 = lc.getClustering();
        Clustering<Integer> c2 = lc.getClustering();

        assertSame(c1, c2);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRejectsNonUndirectedGraph() {
        Graph<Integer, DefaultWeightedEdge> directed =
                new org.jgrapht.graph.DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        directed.addVertex(1);
        directed.addVertex(2);
        directed.addEdge(1, 2);

        assertThrows(IllegalArgumentException.class,
                () -> new LeidenClustering<>(directed, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY));
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRejectsInvalidResolution() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);

        assertThrows(IllegalArgumentException.class,
                () -> new LeidenClustering<>(g, 0.0, new Random(1), LeidenClustering.Quality.MODULARITY));
        assertThrows(IllegalArgumentException.class,
                () -> new LeidenClustering<>(g, Double.NaN, new Random(1), LeidenClustering.Quality.MODULARITY));
        assertThrows(IllegalArgumentException.class,
                () -> new LeidenClustering<>(g, Double.POSITIVE_INFINITY, new Random(1), LeidenClustering.Quality.MODULARITY));
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testRejectsNegativeOrNaNWeights() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        DefaultWeightedEdge e = g.addEdge(1, 2);
        g.setEdgeWeight(e, -1.0);

        LeidenClustering<Integer, DefaultWeightedEdge> lcNeg =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);
        assertThrows(IllegalArgumentException.class, lcNeg::getClustering);

        g.setEdgeWeight(e, Double.NaN);
        LeidenClustering<Integer, DefaultWeightedEdge> lcNan =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);
        assertThrows(IllegalArgumentException.class, lcNan::getClustering);
    }

    /* -------------------------------------------------------------- */
    @Test
    public void testLeidenRepairsDisconnectedCommunityFromLouvainExample() {
        Graph<Integer, DefaultWeightedEdge> g =
                new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Two dense cliques connected only via weak ties through node 0
        int[][] cliques = {{1, 2, 3}, {4, 5, 6}};
        for (int[] clique : cliques) {
            for (int v : clique) {
                g.addVertex(v);
            }
            addClique(g, clique, 4.0);
        }
        g.addVertex(0);
        for (int v = 1; v <= 6; v++) {
            DefaultWeightedEdge e = g.addEdge(0, v);
            g.setEdgeWeight(e, 0.5);
        }

        // External dense community strongly attracting node 0
        int[] external = {7, 8, 9};
        for (int v : external) {
            g.addVertex(v);
        }
        addClique(g, external, 6.0);
        for (int v : external) {
            DefaultWeightedEdge e = g.addEdge(0, v);
            g.setEdgeWeight(e, 8.0);
        }

        double resolution = 0.1; // encourage merging into larger communities
        // Louvain could produce a disconnected community on this graph; ensure Leiden stays connected
        int[] seeds = {0, 1, 2, 3, 4, 42, 4242};
        for (int seed : seeds) {
            LeidenClustering<Integer, DefaultWeightedEdge> leiden =
                    new LeidenClustering<>(g, resolution, new Random(seed), LeidenClustering.Quality.MODULARITY);
            List<Set<Integer>> leidenClusters = leiden.getClustering().getClusters();

            assertTrue(leidenClusters.stream().allMatch(c -> isConnected(g, c)),
                    "Leiden should return only connected communities");
            assertFalse(leidenClusters.stream().anyMatch(c -> c.contains(1) && c.contains(4)),
                    "Leiden should split the disconnected Louvain community");
        }
    }

    private static <V, E> boolean isConnected(Graph<V, E> g, Set<V> vertices) {
        if (vertices.size() <= 1) {
            return true;
        }
        ConnectivityInspector<V, E> inspector =
                new ConnectivityInspector<>(new AsSubgraph<>(g, vertices));
        return inspector.isConnected();
    }

    private static void addClique(Graph<Integer, DefaultWeightedEdge> g, int[] nodes, double weight) {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                DefaultWeightedEdge e = g.addEdge(nodes[i], nodes[j]);
                g.setEdgeWeight(e, weight);
            }
        }
    }
}
