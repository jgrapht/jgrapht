/*
 * (C) Copyright 2015-2018, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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
{
    private final String[] labyrinth1 =
        { ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . ####. . . . . . . . . . . . . . . . ####. . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . ####T . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
            ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
            ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
            "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ." };

    private final String[] labyrinth2 = { // Target node is unreachable
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
        ". . . ####. . . . . . . . . . . . . . . . ####### . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . ####T## . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
        ". . . ####. . . . . . . . ####. . . . . . ##########. . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . ####. . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
        ". . . . . . . . . . . . . ####. . . . . . . . . . . . . . .",
        "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ." };

    private Graph<Node, DefaultWeightedEdge> graph;
    private Node sourceNode;
    private Node targetNode;

    private void readLabyrinth(String[] labyrinth)
    {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Create the nodes
        Node[][] nodes = new Node[labyrinth.length][labyrinth[0].length()];
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[0].length(); j++) {
                if (labyrinth[i].charAt(j) == '#' || labyrinth[i].charAt(j) == ' ')
                    continue;
                nodes[i][j] = new Node(i, j / 2);
                graph.addVertex(nodes[i][j]);
                if (labyrinth[i].charAt(j) == 'S')
                    sourceNode = nodes[i][j];
                else if (labyrinth[i].charAt(j) == 'T')
                    targetNode = nodes[i][j];
            }
        }
        // Create the edges
        // a. Horizontal edges
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[0].length() - 2; j++) {
                if (nodes[i][j] == null || nodes[i][j + 2] == null)
                    continue;
                Graphs.addEdge(graph, nodes[i][j], nodes[i][j + 2], 1);
            }
        }
        // b. Vertical edges
        for (int i = 0; i < labyrinth.length - 1; i++) {
            for (int j = 0; j < labyrinth[0].length(); j++) {
                if (nodes[i][j] == null || nodes[i + 1][j] == null)
                    continue;
                Graphs.addEdge(graph, nodes[i][j], nodes[i + 1][j], 1);
            }
        }
    }

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
        assertTrue(aStarShortestPath.isConsistentHeuristic(new ManhattanDistance()));

        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath2 =
            new AStarShortestPath<>(graph, new EuclideanDistance());
        GraphPath<Node, DefaultWeightedEdge> path2 =
            aStarShortestPath2.getPath(sourceNode, targetNode);
        assertNotNull(path2);
        assertEquals((int) path2.getWeight(), 47);
        assertEquals(path2.getEdgeList().size(), 47);
        assertTrue(aStarShortestPath2.isConsistentHeuristic(new EuclideanDistance()));
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
        assertTrue(aStarShortestPath.isConsistentHeuristic(new ManhattanDistance()));
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
        WeightedMultigraph<Node, DefaultWeightedEdge> multigraph =
            new WeightedMultigraph<>(DefaultWeightedEdge.class);
        Node n1 = new Node(0, 0);
        multigraph.addVertex(n1);
        Node n2 = new Node(1, 0);
        multigraph.addVertex(n2);
        Node n3 = new Node(2, 0);
        multigraph.addVertex(n3);
        Graphs.addEdge(multigraph, n1, n2, 5.0);
        Graphs.addEdge(multigraph, n1, n2, 4.0);
        Graphs.addEdge(multigraph, n1, n2, 8.0);
        Graphs.addEdge(multigraph, n2, n3, 7.0);
        Graphs.addEdge(multigraph, n2, n3, 9);
        Graphs.addEdge(multigraph, n2, n3, 2);
        AStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new AStarShortestPath<>(multigraph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path = aStarShortestPath.getPath(n1, n3);
        assertNotNull(path);
        assertEquals((int) path.getWeight(), 6);
        assertEquals(path.getEdgeList().size(), 2);
        assertTrue(aStarShortestPath.isConsistentHeuristic(new ManhattanDistance()));
    }

    @Test
    public void testInconsistentHeuristic()
    {
        Graph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);

        g.setEdgeWeight(g.addEdge(0, 1), 0.5822723681370429);
        g.setEdgeWeight(g.addEdge(0, 3), 0.8512429683406786);
        g.setEdgeWeight(g.addEdge(3, 0), 0.22867383417976428);
        g.setEdgeWeight(g.addEdge(1, 2), 0.1531858692059932);
        g.setEdgeWeight(g.addEdge(3, 1), 0.9639222864568235);
        g.setEdgeWeight(g.addEdge(2, 2), 0.23262564370920258);
        g.setEdgeWeight(g.addEdge(2, 2), 0.6166416559599189);
        g.setEdgeWeight(g.addEdge(3, 3), 0.6088954021459719);
        g.setEdgeWeight(g.addEdge(3, 3), 0.2476189990121238);

        AStarAdmissibleHeuristic<Integer> h = new AStarAdmissibleHeuristic<Integer>()
        {

            @Override
            public double getCostEstimate(Integer s, Integer t)
            {
                if (s.intValue() == 0 && t.intValue() == 1) {
                    // actual = 0.5822723681370429
                    return 0.5822723681370429;
                }
                if (s.intValue() == 3 && t.intValue() == 1) {
                    // actual = 0.8109462023168071
                    return 0.8109462023168071;
                }
                if (s.intValue() == 3 && t.intValue() == 2) {
                    // actual = 0.9641320715228003
                    return 0.9639222864568235;
                }
                if (s.intValue() == 0 && t.intValue() == 1) {
                    // actual = 0.5822723681370429
                    return 0.5822723681370429;
                }
                if (s.intValue() == 0 && t.intValue() == 2) {
                    // actual = 0.7354582373430361
                    return 0.7354582373430361;
                }

                // all other zero
                return 0d;
            }
        };

        AStarShortestPath<Integer, DefaultWeightedEdge> alg = new AStarShortestPath<>(g, h);
        // shortest path from 3 to 2 is 3->0->1->2 with weight 0.9641320715228003
        assertEquals(0.9641320715228003, alg.getPath(3, 2).getWeight(), 1e-9);
        assertFalse(alg.isConsistentHeuristic(h));
    }

    private class ManhattanDistance
        implements
        AStarAdmissibleHeuristic<Node>
    {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex)
        {
            return Math.abs(sourceVertex.x - targetVertex.x)
                + Math.abs(sourceVertex.y - targetVertex.y);
        }
    }

    private class EuclideanDistance
        implements
        AStarAdmissibleHeuristic<Node>
    {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex)
        {
            return Math.sqrt(
                Math.pow(sourceVertex.x - targetVertex.x, 2)
                    + Math.pow(sourceVertex.y - targetVertex.y, 2));
        }
    }

    private class Node
    {
        public final int x;
        public final int y;

        private Node(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public String toString()
        {
            return "(" + x + "," + y + ")";
        }
    }
}
