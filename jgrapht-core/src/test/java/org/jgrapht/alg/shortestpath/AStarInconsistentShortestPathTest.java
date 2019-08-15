/* (C) Copyright 2015-2018, by Joris Kinable, Brooks Bockman and Contributors.
*/
package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Test class for AStarInconsistentShortestPath implementation
 *
 * @author Joris Kinable
 * @author Brooks Bockman
 */
public class AStarInconsistentShortestPathTest
    extends
    AStarShortestPathTest
{

    /**
     * Test on a graph with a path from the source node to the target node.
     */
    @Override
    @Test
    public void testLabyrinth1()
    {
        this.readLabyrinth(labyrinth1);

        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarInconsistentShortestPath<>(graph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path =
            aStarShortestPath.getPath(sourceNode, targetNode);
        assertNotNull(path);
        assertEquals((int) path.getWeight(), 47);
        assertEquals(path.getEdgeList().size(), 47);
        assertEquals(path.getLength() + 1, 48);

        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath2 =
            new AStarShortestPath<>(graph, new EuclideanDistance());
        GraphPath<Node, DefaultWeightedEdge> path2 =
            aStarShortestPath2.getPath(sourceNode, targetNode);
        assertNotNull(path2);
        assertEquals((int) path2.getWeight(), 47);
        assertEquals(path2.getEdgeList().size(), 47);
    }

    /**
     * Test on a graph where there is no path from the source node to the target node.
     */
    @Override
    @Test
    public void testLabyrinth2()
    {
        this.readLabyrinth(labyrinth2);
        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarInconsistentShortestPath<>(graph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path =
            aStarShortestPath.getPath(sourceNode, targetNode);
        assertNull(path);
    }

    /**
     * This test verifies whether multigraphs are processed correctly. In a multigraph, there are
     * multiple edges between the same vertex pair. Each of these edges can have a different cost.
     * Here we create a simple multigraph A-B-C with multiple edges between (A,B) and (B,C) and
     * query the shortest path, which is simply the cheapest edge between (A,B) plus the cheapest
     * edge between (B,C). The admissible heuristic in this test is not important.
     */
    @Override
    @Test
    public void testMultiGraph()
    {
        Graph<Node, DefaultWeightedEdge> multigraph = getMultigraph();
        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarInconsistentShortestPath<>(multigraph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path = aStarShortestPath.getPath(n1, n3);
        assertNotNull(path);
        assertEquals((int) path.getWeight(), 6);
        assertEquals(path.getEdgeList().size(), 2);
    }
    
    @Override
    @Test
    public void testInconsistentHeuristic()
    {
        Graph<Integer, DefaultWeightedEdge> g = getInconsistentHeuristicTestGraph();
        AStarAdmissibleHeuristic<Integer> h = getInconsistentHeuristic();

        AStarShortestPath<Integer, DefaultWeightedEdge> alg = new AStarInconsistentShortestPath<>(g, h);

        // shortest path from 3 to 2 is 3->0->1->2 with weight 0.9641320715228003
        assertEquals(0.9641320715228003, alg.getPath(3, 2).getWeight(), 1e-9);
    }
}
