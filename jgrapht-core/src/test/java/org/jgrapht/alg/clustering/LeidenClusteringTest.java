package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for LeidenClustering.
 *
 * Path:
 *   jgrapht-core/src/test/java/org/jgrapht/alg/clustering/LeidenClusteringTest.java
 *
 * These tests check:
 *  - correctness on simple graphs
 *  - refinement (communities must be connected)
 *  - CPM vs modularity behavior
 *  - deterministic behavior with fixed RNG
 *  - handling of self-loops & empty graphs
 */
public class LeidenClusteringTest
{
    /* ---------- Helpers ---------- */

    private Graph<Integer, DefaultEdge> newUndirectedGraph() {
        return GraphTypeBuilder
            .undirected()
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
            .buildGraph();
    }

    private Graph<Integer, DefaultEdge> newUndirectedWithSelfLoops() {
        return GraphTypeBuilder
            .undirected()
            .allowingSelfLoops(true)
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier())
            .buildGraph();
    }

    /**
     * Checks that every community is internally connected (Leiden refinement guarantee).
     */
    private void assertCommunitiesConnected(
        Graph<Integer, DefaultEdge> g,
        ClusteringAlgorithm.Clustering<Integer> clustering)
    {
        for (Set<Integer> community : clustering.getClusters()) {
            if (community.size() <= 1)
                continue;

            // BFS from an arbitrary vertex
            Integer start = community.iterator().next();
            Set<Integer> visited = new HashSet<>();
            Deque<Integer> dq = new ArrayDeque<>();
            dq.add(start);
            visited.add(start);

            while (!dq.isEmpty()) {
                Integer u = dq.removeFirst();
                for (DefaultEdge e : g.edgesOf(u)) {
                    Integer v = g.getEdgeSource(e).equals(u)
                        ? g.getEdgeTarget(e)
                        : g.getEdgeSource(e);

                    if (community.contains(v) && visited.add(v)) {
                        dq.add(v);
                    }
                }
            }

            assertEquals(
                community,
                visited,
                "Leiden refinement requires each community to induce a connected subgraph"
            );
        }
    }

    /* ---------- Tests ---------- */

    @Test
    public void testSingleNode() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        g.addVertex(1);

        LeidenClustering<Integer, DefaultEdge> alg =
            new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);

        var clustering = alg.getClustering();

        assertEquals(1, clustering.getNumberClusters());
        assertEquals(Set.of(1), clustering.getClusters().get(0));
    }

    @Test
    public void testTwoNodesOneEdge() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        g.addVertex(1);
        g.addVertex(2);
        g.addEdge(1, 2);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertEquals(1, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2), new HashSet<>(clustering.getClusters().get(0)));
    }

    @Test
    public void testTwoNodesNoEdge() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        g.addVertex(1);
        g.addVertex(2);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        // No edges ⇒ two singleton communities
        assertEquals(2, clustering.getNumberClusters());
        assertTrue(clustering.getClusters().contains(Set.of(1)));
        assertTrue(clustering.getClusters().contains(Set.of(2)));
    }

    @Test
    public void testTriangleClique() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertEquals(1, clustering.getNumberClusters());
        assertEquals(Set.of(1, 2, 3), clustering.getClusters().get(0));
    }

    @Test
    public void testTwoCliquesDisconnected() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();

        // Clique 1
        for (int i = 1; i <= 5; i++) g.addVertex(i);
        for (int i = 1; i <= 5; i++)
            for (int j = i + 1; j <= 5; j++)
                g.addEdge(i, j);

        // Clique 2
        for (int i = 6; i <= 10; i++) g.addVertex(i);
        for (int i = 6; i <= 10; i++)
            for (int j = i + 1; j <= 10; j++)
                g.addEdge(i, j);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        assertCommunitiesConnected(g, clustering);
    }

    @Test
    public void testWeakBridge_Modularity_vs_CPM() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();

        // Community A
        for (int i = 1; i <= 5; i++) g.addVertex(i);
        g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 4); g.addEdge(4, 5); g.addEdge(5, 1);
        g.addEdge(2, 4);

        // Community B
        for (int i = 6; i <= 10; i++) g.addVertex(i);
        g.addEdge(6, 7); g.addEdge(7, 8); g.addEdge(8, 9); g.addEdge(9, 10); g.addEdge(10, 6);
        g.addEdge(7, 9);

        // Weak bridge
        g.addEdge(5, 6);

        var modAlg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var cpmAlg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.CPM);

        var modC = modAlg.getClustering();
        var cpmC = cpmAlg.getClustering();

        assertTrue(modC.getNumberClusters() >= 2);
        assertTrue(cpmC.getNumberClusters() >= 2);
    }

    @Test
    public void testNoEdgesTenNodes() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        for (int i = 1; i <= 10; i++) g.addVertex(i);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertEquals(10, clustering.getNumberClusters());
    }

    @Test
    public void testSelfLoops() {
        Graph<Integer, DefaultEdge> g = newUndirectedWithSelfLoops();
        g.addVertex(1); g.addEdge(1, 1);

        g.addVertex(2); g.addVertex(3); g.addVertex(4);
        g.addEdge(2, 3); g.addEdge(3, 4); g.addEdge(4, 2);

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertCommunitiesConnected(g, clustering);
    }

    @Test
    public void testDeterminismWithFixedSeed() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();

        for (int i = 1; i <= 6; i++) g.addVertex(i);

        g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 1);
        g.addEdge(4, 5); g.addEdge(5, 6); g.addEdge(6, 4);
        g.addEdge(3, 4);

        var alg1 = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var alg2 = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);

        var c1 = alg1.getClustering();
        var c2 = alg2.getClustering();

        assertEquals(new HashSet<>(c1.getClusters()), new HashSet<>(c2.getClusters()));
    }

    @Test
    public void testRefinementSplitsDisconnectedSets() {
        Graph<Integer, DefaultEdge> g = newUndirectedGraph();
        g.addVertex(1); g.addVertex(2); g.addVertex(3); g.addVertex(4);

        g.addEdge(1, 2); // component 1
        g.addEdge(3, 4); // component 2

        var alg = new LeidenClustering<>(g, 1.0, new Random(42), LeidenClustering.Quality.MODULARITY);
        var clustering = alg.getClustering();

        assertEquals(2, clustering.getNumberClusters());
        assertCommunitiesConnected(g, clustering);
    }
}
