package org.jgrapht.alg.tour;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.TestUtil;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Test;

import java.util.List;

import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link FarthestInsertionHeuristicTSP}
 *
 * @author Jose Alejandro Cornejo Acosta
 */
public class FarthestInsertionHeuristicTSPTest
{
    /**
     * Directed graph
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        farthestInsertion.getTour(graph);
    }

    /**
     * Empty graph
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        farthestInsertion.getTour(graph);
    }

    /**
     * Not complete
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNoCompleteGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(0);
        graph.addVertex(1);
        FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        farthestInsertion.getTour(graph);
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour1()
    {
        int[][] edges = {{1, 2, 5}};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        FarthestInsertionHeuristicTSP<Integer, DefaultEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        GraphPath<Integer, DefaultEdge> tour = farthestInsertion.getTour(graph);
        assertHamiltonian(graph, tour);
        assertEquals(10, tour.getWeight(), 1e-9);
    }

    /**
     * There is only one tour
     */
    @Test
    public void testGetTour2()
    {
        int[][] edges = {{1, 2, 5}, {1, 3, 5}, {2, 3, 9},};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        FarthestInsertionHeuristicTSP<Integer, DefaultEdge> farthestInsertion =
            new FarthestInsertionHeuristicTSP<>();
        GraphPath<Integer, DefaultEdge> tour = farthestInsertion.getTour(graph);
        assertHamiltonian(graph, tour);
        assertEquals(19, tour.getWeight(), 1e-9);
    }

    /**
     * Test with dummy graph of five vertices
     */
    @Test
    public void testDummyGraph5()
    {
        int[][] D = {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> G = createGraphFromMatrixDistances(D);
        var farthestInsH = new FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge>();

        var tour = farthestInsH.getTour(G);
        assertEquals(30, tour.getWeight(), 1e-9);
        assertArrayEquals(new Integer[]{3, 2, 1, 0, 4, 3},
            tour.getVertexList().toArray(new Integer[0]));
    }

    @Test
    public void testDummyGraph5WithSubtour()
    {
        int[][] D = {{0, 8, 10, 11, 15},
            {8, 0, 2, 3, 7},
            {10, 2, 0, 1, 5},
            {11, 3, 1, 0, 4},
            {15, 7, 5, 4, 0}
        };
        Graph<Integer, DefaultWeightedEdge> G = createGraphFromMatrixDistances(D);
        var farthestInsH = new FarthestInsertionHeuristicTSP
            <Integer, DefaultWeightedEdge>(new GraphWalk<>(G, List.of(3, 2, 0, 4), -1));

        var tour = farthestInsH.getTour(G);
        assertEquals(30, tour.getWeight(), 1e-9);

        // vertex 1 should be inserted between vertices 2 and 0
        assertArrayEquals(new Integer[]{3, 2, 1, 0, 4, 3},
            tour.getVertexList().toArray(new Integer[0]));
    }

    /**
     * Test with dummy graph of ten vertices
     */
    @Test
    public void testDummyGraph10()
    {
        int[][] D = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 0, 10, 11, 12, 13, 14, 15, 16, 17},
            {2, 10, 0, 18, 19, 20, 21, 22, 23, 24},
            {3, 11, 18, 0, 25, 26, 27, 28, 29, 30},
            {4, 12, 19, 25, 0, 31, 32, 33, 34, 35},
            {5, 13, 20, 26, 31, 0, 36, 37, 38, 39},
            {6, 14, 21, 27, 32, 36, 0, 40, 41, 42},
            {7, 15, 22, 28, 33, 37, 40, 0, 43, 44},
            {8, 16, 23, 29, 34, 38, 41, 43, 0, 45},
            {9, 17, 24, 30, 35, 39, 42, 44, 45, 0}};

        Graph<Integer, DefaultWeightedEdge> G = createGraphFromMatrixDistances(D);
        var farthestInsertion = new FarthestInsertionHeuristicTSP<Integer, DefaultWeightedEdge>();
        var tour = farthestInsertion.getTour(G);
        assertEquals(210, tour.getWeight(), 1e-9);
        assertArrayEquals(new Integer[]{4, 5, 1, 6, 0, 7, 3, 8, 2, 9, 4},
            tour.getVertexList().toArray(new Integer[0]));
    }

    // utilities
    static Graph<Integer, DefaultWeightedEdge> createGraphFromMatrixDistances(int[][] D)
    {
        int n = D.length;
        var G = GraphTypeBuilder
            .<Integer, DefaultWeightedEdge>undirected().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();

        // add edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (i != j) {
                    Graphs.addEdgeWithVertices(G, i, j, D[i][j]);
                }
            }
        }
        return G;
    }
}
