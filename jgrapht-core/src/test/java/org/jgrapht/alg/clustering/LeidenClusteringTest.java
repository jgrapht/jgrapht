package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LeidenClusteringTest {

    /* =========================================================
       BASIC GRAPH EDGE CASES
       ========================================================= */

    @Test
    public void testNoEdges() {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        LeidenClustering<Integer, DefaultEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);

        var clusters = lc.getClustering().getClusters();

        assertEquals(3, clusters.size(), "Disconnected vertices must remain singleton clusters.");
    }

    @Test
    public void testSingleVertex() {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(42);

        LeidenClustering<Integer, DefaultEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);

        var clusters = lc.getClustering().getClusters();

        assertEquals(1, clusters.size());
        assertTrue(clusters.get(0).contains(42));
    }

    @Test
    public void testCompleteGraphSingleCommunity() {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 0; i < 5; i++) g.addVertex(i);
        for (int i = 0; i < 5; i++)
            for (int j = i + 1; j < 5; j++)
                g.addEdge(i, j);

        LeidenClustering<Integer, DefaultEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);

        var clusters = lc.getClustering().getClusters();

        assertEquals(1, clusters.size(), "Complete graph must produce exactly one community.");
    }

    /* =========================================================
       REFINEMENT TESTS
       ========================================================= */

    @Test
    public void testRefinementSplitsDisconnectedCommunity() {
        // Community {1,2,3,4} but internally split as {1,2} and {3,4}
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 4; i++) g.addVertex(i);

        g.addEdge(1, 2); // component 1
        g.addEdge(3, 4); // component 2

        // Leiden must split into two components
        LeidenClustering<Integer, DefaultEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);

        var clusters = lc.getClustering().getClusters();

        assertEquals(2, clusters.size(), "Disconnected components inside same community MUST be split.");

        boolean found12 = clusters.stream().anyMatch(s -> s.containsAll(Set.of(1, 2)));
        boolean found34 = clusters.stream().anyMatch(s -> s.containsAll(Set.of(3, 4)));

        assertTrue(found12 && found34, "Refinement must detect {1,2} and {3,4} as separate clusters.");
    }

    /* =========================================================
       WEIGHTED CPM TEST
       ========================================================= */

    @Test
    public void testWeightedCPM() {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        g.setEdgeWeight(g.addEdge(1, 2), 5); // strong
        g.setEdgeWeight(g.addEdge(2, 3), 1); // weak

        LeidenClustering<Integer, DefaultWeightedEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.CPM);

        var clusters = lc.getClustering().getClusters();

        assertTrue(
                clusters.stream().anyMatch(s -> s.containsAll(Set.of(1, 2))),
                "CPM must prefer {1,2} due to strong edge weight."
        );
    }

    /* =========================================================
       CRITICAL TEST: LOUVAIN FAILURE EXAMPLE (Leiden paper)
       ========================================================= */

    @Test
    public void testLeidenFixesLouvainDisconnectedCommunity() {

        SimpleWeightedGraph<Integer, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices 0–6
        for (int i = 0; i <= 6; i++) g.addVertex(i);

        // Strong edges from node 0 (per Appendix B)
        g.setEdgeWeight(g.addEdge(0, 1), 2);
        g.setEdgeWeight(g.addEdge(0, 4), 2);

        // Internal weak triangles
        int[][] weakTriangles = {
                {1, 2}, {2, 3}, {3, 1},
                {4, 5}, {5, 6}, {6, 4}
        };
        for (int[] e : weakTriangles)
            g.setEdgeWeight(g.addEdge(e[0], e[1]), 1);

        // External nodes attracting 0 outward
        for (int i = 10; i < 15; i++) {
            g.addVertex(i);
            g.setEdgeWeight(g.addEdge(0, i), 2);
        }

        LeidenClustering<Integer, DefaultWeightedEdge> lc =
                new LeidenClustering<>(g, 1.0, new Random(1), LeidenClustering.Quality.MODULARITY);

        var clusters = lc.getClustering().getClusters();

        boolean has123 = clusters.stream().anyMatch(c -> c.containsAll(Set.of(1, 2, 3)));
        boolean has456 = clusters.stream().anyMatch(c -> c.containsAll(Set.of(4, 5, 6)));

        assertTrue(has123, "Leiden must recover subcommunity {1,2,3} from Fig.2");
        assertTrue(has456, "Leiden must recover subcommunity {4,5,6} from Fig.2");
    }


    /* =========================================================
       FULL PIPELINE TEST — CPM WITH SMALLER GAMMA
       ========================================================= */

    @Test
    public void testTwoBlocks() {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        // Block 1: cycle of 4 nodes
        for (int i = 1; i <= 4; i++) g.addVertex(i);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 1);

        // Block 2: another cycle of 4 nodes
        for (int i = 5; i <= 8; i++) g.addVertex(i);
        g.addEdge(5, 6);
        g.addEdge(6, 7);
        g.addEdge(7, 8);
        g.addEdge(8, 5);

        // No edges between blocks

        // Use CPM with smaller gamma so that ΔQ = k_u_in − γ*k_u > 0
        // For each edge: k_u_in = 1, k_u = 2, so need γ < 0.5.
        double gamma = 0.2;

        LeidenClustering<Integer, DefaultEdge> lc =
                new LeidenClustering<>(g, gamma, new Random(1), LeidenClustering.Quality.CPM);

        var clusters = lc.getClustering().getClusters();

        assertEquals(2, clusters.size(),
                "Two disconnected blocks must produce exactly two CPM communities at small γ.");

        boolean block1 = clusters.stream().anyMatch(s -> s.containsAll(Set.of(1, 2, 3, 4)));
        boolean block2 = clusters.stream().anyMatch(s -> s.containsAll(Set.of(5, 6, 7, 8)));

        assertTrue(block1, "Block 1 {1,2,3,4} must form a community.");
        assertTrue(block2, "Block 2 {5,6,7,8} must form a community.");
    }
}
