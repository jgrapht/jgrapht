package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.TestUtil;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.PlantedPartitionGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.util.SupplierUtil;
import java.util.*;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for OverlappingClustering
 */
public class OverlappingClusteringTest
{
    @Test
    public void testFriendshipGroups()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});

        OverlappingClustering<Integer, DefaultEdge> alg = new OverlappingClustering<>(g, 2);
        alg.findFriendshipGroups();

        Set<Set<Integer>> expectedFriendshipGroups = new HashSet<>();
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 1)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 2)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 3, 4)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(3, 4, 5)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 3, 4, 5)));

        // Parallel fashion requires the check below to see if the result equals the expected result.
        assertTrue(alg.getFriendshipGroups().containsAll(expectedFriendshipGroups) && alg.getFriendshipGroups().size()==expectedFriendshipGroups.size());
    }

    @Test
    public void testProperSubsetRemoval()
    {
        Graph<Integer, DefaultEdge> g = TestUtil
                .createUndirected(new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {3, 4}, {3, 5}, {4, 5}});

        OverlappingClustering<Integer, DefaultEdge> alg = new OverlappingClustering<>(g, 2);
        alg.findFriendshipGroups();
        alg.removeProperSubsets();

        Set<Set<Integer>> expectedFriendshipGroups = new HashSet<>();
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 1)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 2)));
        expectedFriendshipGroups.add(new HashSet<>(Arrays.asList(0, 3, 4, 5)));

        // Parallel fashion requires the check below to see if the result equals the expected result.
        assertTrue(alg.getSuperGroups().containsAll(expectedFriendshipGroups) && alg.getSuperGroups().size()==expectedFriendshipGroups.size());
    }

    @Test
    public void testSmallGraph()
    {
        Graph<Integer, DefaultEdge> graph = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        for(int i=0; i<20; i++)
        {
            graph.addVertex();
        }
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(2, 19);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(4, 8);
        graph.addEdge(5, 8);
        graph.addEdge(5, 6);
        graph.addEdge(6, 7);
        graph.addEdge(7, 8);
        graph.addEdge(8, 9);
        graph.addEdge(9, 10);
        graph.addEdge(9, 11);
        graph.addEdge(9, 12);
        graph.addEdge(10, 11);
        graph.addEdge(12, 13);
        graph.addEdge(12, 15);
        graph.addEdge(12, 16);
        graph.addEdge(13, 15);
        graph.addEdge(13, 14);
        graph.addEdge(14, 15);
        graph.addEdge(16, 19);
        graph.addEdge(16, 17);
        graph.addEdge(16, 18);
        graph.addEdge(17, 19);
        graph.addEdge(17, 18);
        graph.addEdge(18, 19);

        OverlappingClustering<Integer, DefaultEdge> alg = new OverlappingClustering<>(graph, OverlappingClustering.DEFAULT_RADIUS);

        List<Set<Integer>> expectedClusters = new ArrayList<>();
        expectedClusters.add(Set.of(0, 1, 2, 3, 4));
        expectedClusters.add(Set.of(16, 17, 18, 2, 19));
        expectedClusters.add(Set.of(4, 5, 6, 7, 8, 9, 12));
        expectedClusters.add(Set.of(16, 12, 13, 14, 15));
        expectedClusters.add(Set.of(9, 10, 11));
        Clustering<Integer> expectedClustering = new ClusteringImpl<>(new ArrayList<>(expectedClusters));

        Clustering<Integer> clustering = alg.getClustering();
        assertEquals(expectedClustering.getClusters(), clustering.getClusters());
    }

    @Test
    public void testMediumGraph()
    {
        Graph<Integer, DefaultEdge> graph = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();
        for(int i=0; i<28; i++)
        {
            graph.addVertex();
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);
        graph.addEdge(2, 3);
        graph.addEdge(2, 5);
        graph.addEdge(3, 4);
        graph.addEdge(3, 26);
        graph.addEdge(5, 6);
        graph.addEdge(5, 7);
        graph.addEdge(6, 7);
        graph.addEdge(7, 8);
        graph.addEdge(8, 9);
        graph.addEdge(8, 12);
        graph.addEdge(9, 10);
        graph.addEdge(9, 11);
        graph.addEdge(9, 12);
        graph.addEdge(10, 11);
        graph.addEdge(10, 12);
        graph.addEdge(11, 12);
        graph.addEdge(11, 13);
        graph.addEdge(13, 17);
        graph.addEdge(13, 16);
        graph.addEdge(13, 14);
        graph.addEdge(17, 16);
        graph.addEdge(18, 16);
        graph.addEdge(15, 16);
        graph.addEdge(15, 18);
        graph.addEdge(17, 18);
        graph.addEdge(14, 16);
        graph.addEdge(16, 19);
        graph.addEdge(19, 20);
        graph.addEdge(19, 21);
        graph.addEdge(19, 22);
        graph.addEdge(19, 23);
        graph.addEdge(20, 21);
        graph.addEdge(21, 22);
        graph.addEdge(22, 23);
        graph.addEdge(23, 24);
        graph.addEdge(24, 25);
        graph.addEdge(24, 26);
        graph.addEdge(24, 27);
        graph.addEdge(26, 25);
        graph.addEdge(27, 26);

        List<Set<Integer>> expectedClusters = new ArrayList<>();
        expectedClusters.add(Set.of(5, 6, 7, 8));
        expectedClusters.add(Set.of(0, 1, 2, 3, 4, 5, 26));
        expectedClusters.add(Set.of(16, 17, 18, 19, 11, 13, 14, 15));
        expectedClusters.add(Set.of(23, 24, 25, 26, 27));
        expectedClusters.add(Set.of(19, 20, 21, 22, 23));
        expectedClusters.add(Set.of(8, 9, 10, 11, 12));
        Clustering<Integer> expectedClustering = new ClusteringImpl<>(new ArrayList<>(expectedClusters));

        OverlappingClustering<Integer, DefaultEdge> alg = new OverlappingClustering<>(graph, OverlappingClustering.DEFAULT_RADIUS);
        Clustering<Integer> clustering = alg.getClustering();
        assertEquals(expectedClustering.getClusters(), clustering.getClusters());
    }

    @Test
    public void testBigGraph()
    {
        Graph<Integer, DefaultEdge> graph = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        GraphGenerator<Integer, DefaultEdge, Integer> generator =
                new PlantedPartitionGraphGenerator<>(10, 10, 0.40, 0.01, 5);
        generator.generateGraph(graph);

        OverlappingClustering<Integer, DefaultEdge> alg = new OverlappingClustering<>(graph, OverlappingClustering.DEFAULT_RADIUS);
        Clustering<Integer> clustering = alg.getClustering();
        System.out.print(clustering);
    }
}

