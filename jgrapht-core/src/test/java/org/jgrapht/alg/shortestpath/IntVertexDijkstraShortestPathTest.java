package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.WeightedPseudograph;
import org.junit.Test;

/**
 * Tests for {@link IntVertexDijkstraShortestPath}.
 * 
 * @author Dimitrios Michail
 */
public class IntVertexDijkstraShortestPathTest
{
    @Test
    public void testUndirected()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3, 4));
        g.setEdgeWeight(g.addEdge(0, 1), 2.0);
        g.setEdgeWeight(g.addEdge(0, 2), 3.0);
        g.setEdgeWeight(g.addEdge(0, 4), 100.0);
        g.setEdgeWeight(g.addEdge(1, 3), 5.0);
        g.setEdgeWeight(g.addEdge(2, 3), 20.0);
        g.setEdgeWeight(g.addEdge(3, 4), 5.0);

        IntVertexDijkstraShortestPath<DefaultWeightedEdge> algo =
            new IntVertexDijkstraShortestPath<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source0 = algo.getPaths(0);
        assertEquals(source0.getWeight(0), 0d, 1e-9);
        assertEquals(source0.getWeight(1), 2d, 1e-9);
        assertEquals(source0.getWeight(2), 3d, 1e-9);
        assertEquals(source0.getWeight(3), 7d, 1e-9);
        assertEquals(source0.getWeight(4), 12d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source1 = algo.getPaths(1);
        assertEquals(source1.getWeight(0), 2d, 1e-9);
        assertEquals(source1.getWeight(1), 0d, 1e-9);
        assertEquals(source1.getWeight(2), 5d, 1e-9);
        assertEquals(source1.getWeight(3), 5d, 1e-9);
        assertEquals(source1.getWeight(4), 10d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source2 = algo.getPaths(2);
        assertEquals(source2.getWeight(0), 3d, 1e-9);
        assertEquals(source2.getWeight(1), 5d, 1e-9);
        assertEquals(source2.getWeight(2), 0d, 1e-9);
        assertEquals(source2.getWeight(3), 10d, 1e-9);
        assertEquals(source2.getWeight(4), 15d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source3 = algo.getPaths(3);
        assertEquals(source3.getWeight(0), 7d, 1e-9);
        assertEquals(source3.getWeight(1), 5d, 1e-9);
        assertEquals(source3.getWeight(2), 10d, 1e-9);
        assertEquals(source3.getWeight(3), 0d, 1e-9);
        assertEquals(source3.getWeight(4), 5d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source4 = algo.getPaths(4);
        assertEquals(source4.getWeight(0), 12d, 1e-9);
        assertEquals(source4.getWeight(1), 10d, 1e-9);
        assertEquals(source4.getWeight(2), 15d, 1e-9);
        assertEquals(source4.getWeight(3), 5d, 1e-9);
        assertEquals(source4.getWeight(4), 0d, 1e-9);
    }

    @Test
    public void testUndirectedWithIdMap()
    {
        WeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList(100, 1, 2, 3, 4));
        g.setEdgeWeight(g.addEdge(100, 1), 2.0);
        g.setEdgeWeight(g.addEdge(100, 2), 3.0);
        g.setEdgeWeight(g.addEdge(100, 4), 100.0);
        g.setEdgeWeight(g.addEdge(1, 3), 5.0);
        g.setEdgeWeight(g.addEdge(2, 3), 20.0);
        g.setEdgeWeight(g.addEdge(3, 4), 5.0);

        IntVertexDijkstraShortestPath<DefaultWeightedEdge> algo =
            new IntVertexDijkstraShortestPath<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source100 = algo.getPaths(100);
        assertEquals(source100.getWeight(100), 0d, 1e-9);
        assertEquals(source100.getWeight(1), 2d, 1e-9);
        assertEquals(source100.getWeight(2), 3d, 1e-9);
        assertEquals(source100.getWeight(3), 7d, 1e-9);
        assertEquals(source100.getWeight(4), 12d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source1 = algo.getPaths(1);
        assertEquals(source1.getWeight(100), 2d, 1e-9);
        assertEquals(source1.getWeight(1), 0d, 1e-9);
        assertEquals(source1.getWeight(2), 5d, 1e-9);
        assertEquals(source1.getWeight(3), 5d, 1e-9);
        assertEquals(source1.getWeight(4), 10d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source2 = algo.getPaths(2);
        assertEquals(source2.getWeight(100), 3d, 1e-9);
        assertEquals(source2.getWeight(1), 5d, 1e-9);
        assertEquals(source2.getWeight(2), 0d, 1e-9);
        assertEquals(source2.getWeight(3), 10d, 1e-9);
        assertEquals(source2.getWeight(4), 15d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source3 = algo.getPaths(3);
        assertEquals(source3.getWeight(100), 7d, 1e-9);
        assertEquals(source3.getWeight(1), 5d, 1e-9);
        assertEquals(source3.getWeight(2), 10d, 1e-9);
        assertEquals(source3.getWeight(3), 0d, 1e-9);
        assertEquals(source3.getWeight(4), 5d, 1e-9);

        SingleSourcePaths<Integer, DefaultWeightedEdge> source4 = algo.getPaths(4);
        assertEquals(source4.getWeight(100), 12d, 1e-9);
        assertEquals(source4.getWeight(1), 10d, 1e-9);
        assertEquals(source4.getWeight(2), 15d, 1e-9);
        assertEquals(source4.getWeight(3), 5d, 1e-9);
        assertEquals(source4.getWeight(4), 0d, 1e-9);
    }

    @Test
    public void testNonNegativeWeights()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2));

        DefaultWeightedEdge we12 = g.addEdge(1, 2);
        g.setEdgeWeight(we12, -100.0);

        try {
            new IntVertexDijkstraShortestPath<>(g).getPath(1, 2);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

}
