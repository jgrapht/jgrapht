package org.jgrapht.demo;

import com.mxgraph.model.mxCell;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.clustering.LeidenClustering;
import org.jgrapht.alg.clustering.LeidenClustering.Quality;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LeidenClusteringDemoTest {

    private Graph<String, DefaultEdge> createSampleGraph() {
        return new LeidenClusteringDemo().createSampleGraph();
    }

    @Test
    void leidenFindsThreeCommunitiesOnSampleGraph() {
        Graph<String, DefaultEdge> graph = createSampleGraph();
        double resolution = 1.0;
        Quality quality = Quality.MODULARITY;

        LeidenClustering<String, DefaultEdge> leiden =
            new LeidenClustering<>(graph, resolution, new Random(42), quality);

        ClusteringAlgorithm.Clustering<String> clustering = leiden.getClustering();

        assertNotNull(clustering);
        assertEquals(3, clustering.getNumberClusters(), "Expected 3 communities");

        for (Set<String> cluster : clustering.getClusters()) {
            assertTrue(cluster.size() >= 4, "Unexpected small community: " + cluster);
        }
    }

    @Test
    void sampleGraphStructureMatchesDemoIntent() {
        Graph<String, DefaultEdge> g = createSampleGraph();

        assertEquals(15, g.vertexSet().size(), "Vertex count mismatch");
        assertEquals(26, g.edgeSet().size(), "Edge count mismatch");
        assertTrue(GraphTests.isSimple(g), "Graph should be simple (no self-loops or multiedges)");

        assertTrue(g.containsEdge("v4", "v5"));
        assertTrue(g.containsEdge("v9", "v10"));
    }

    @Test
    void leidenWithFixedSeedIsDeterministicAndMatchesExpectedCommunities() {
        Graph<String, DefaultEdge> graph = createSampleGraph();
        double resolution = 1.0;
        Quality quality = Quality.MODULARITY;

        LeidenClustering<String, DefaultEdge> run1 =
            new LeidenClustering<>(graph, resolution, new Random(42), quality);
        LeidenClustering<String, DefaultEdge> run2 =
            new LeidenClustering<>(graph, resolution, new Random(42), quality);

        Set<Set<String>> clusters1 = toCanonical(run1.getClustering());
        Set<Set<String>> clusters2 = toCanonical(run2.getClustering());

        assertEquals(clusters1, clusters2, "Same seed should produce identical clustering");

        Set<Set<String>> expected = Set.of(
            setOf("v0", "v1", "v2", "v3", "v4"),
            setOf("v5", "v6", "v7", "v8", "v9"),
            setOf("v10", "v11", "v12", "v13", "v14")
        );

        assertEquals(expected, clusters1, "Detected communities differ from expected partition");
    }

    @Test
    void colorApplicationKeepsCommunitiesConsistentlyStyled() {
        Graph<String, DefaultEdge> graph = createSampleGraph();
        LeidenClusteringDemo demo = new LeidenClusteringDemo();
        ListenableGraph<String, DefaultEdge> listenable = new DefaultListenableGraph<>(graph);
        JGraphXAdapter<String, DefaultEdge> adapter = demo.createAdapter(listenable);

        LeidenClustering<String, DefaultEdge> leiden =
            new LeidenClustering<>(graph, 1.0, new Random(42), Quality.MODULARITY);
        ClusteringAlgorithm.Clustering<String> clustering = leiden.getClustering();

        demo.colorVerticesByCommunity(clustering);

        for (Set<String> cluster : clustering.getClusters()) {
            String expectedFill = null;
            for (String vertex : cluster) {
                mxCell cell = (mxCell) adapter.getVertexToCellMap().get(vertex);
                assertNotNull(cell, "Missing cell for vertex " + vertex);
                String fill = extractFillColor(cell.getStyle());
                assertNotNull(fill, "fillColor not applied for vertex " + vertex);
                if (expectedFill == null) {
                    expectedFill = fill;
                } else {
                    assertEquals(expectedFill, fill, "Cluster should share the same fill color");
                }
            }
        }
    }

    private String extractFillColor(String style) {
        return Arrays.stream(style.split(";"))
            .filter(s -> s.startsWith("fillColor="))
            .map(s -> s.substring("fillColor=".length()))
            .findFirst()
            .orElse(null);
    }

    private Set<Set<String>> toCanonical(ClusteringAlgorithm.Clustering<String> clustering) {
        return clustering.getClusters().stream()
            .map(cluster -> new TreeSet<>(cluster))
            .collect(Collectors.toSet());
    }

    private Set<String> setOf(String... elements) {
        return new TreeSet<>(Set.of(elements));
    }
}
