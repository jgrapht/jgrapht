/*
 * (C) Copyright 2015-2021, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Test class for AStarShortestPath implementation
 *
 * @author Joris Kinable
 */
public class AStarShortestPathTest
    extends
    BaseHeuristicSearchTest
{

    /**
     * Test on a graph with a path from the source node to the target node.
     */
    @Test
    public void testLabyrinth1()
    {
        this.readLabyrinth(labyrinth1);

        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarShortestPath<>(graph, new ManhattanDistance());
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
    @Test
    public void testLabyrinth2()
    {
        this.readLabyrinth(labyrinth2);
        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarShortestPath<>(graph, new ManhattanDistance());
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
    @Test
    public void testMultiGraph()
    {
        Graph<Node, DefaultWeightedEdge> multigraph = getMultigraph();
        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarShortestPath<>(multigraph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path = aStarShortestPath.getPath(n1, n3);
        assertNotNull(path);
        assertEquals((int) path.getWeight(), 6);
        assertEquals(path.getEdgeList().size(), 2);
    }

    @Test
    public void testInconsistentHeuristic()
    {
        Graph<Integer, DefaultWeightedEdge> g = getInconsistentHeuristicTestGraph();
        AStarAdmissibleHeuristic<Integer> h = getInconsistentHeuristic();

        AStarShortestPath<Integer, DefaultWeightedEdge> alg = new AStarShortestPath<>(g, h);

        // shortest path from 3 to 2 is 3->0->1->2 with weight 0.9641320715228003
        assertEquals(0.9641320715228003, alg.getPath(3, 2).getWeight(), 1e-9);
    }
}
