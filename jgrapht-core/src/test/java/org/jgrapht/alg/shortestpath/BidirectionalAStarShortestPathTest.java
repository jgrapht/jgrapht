/*
 * (C) Copyright 2015-2018, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BidirectionalAStarShortestPathTest {
    private final String[] labyrinth1 =
            {". . . . . . . . . . . . . . . . . . . . . ####. . . . . . .",
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
                    "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ."};

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
            "S . . . . . . . . . . . . ####. . . . . . . . . . . . . . ."};

    private Graph<Node, DefaultWeightedEdge> graph;
    private Node sourceNode;
    private Node targetNode;
    private static final String s = "s";
    private static final String t = "t";
    private static final String y = "y";
    private static final String x = "x";
    private static final String z = "z";


    private void readLabyrinth(String[] labyrinth) {
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

    @Test
    public void testEmptyGraph() {
        Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(s);

        new BidirectionalAStarShortestPath<>(graph, (sourceVertex, targetVertex) -> 0).getPaths(s);
    }

    @Test
    public void testNegativeWeightEdge() {
        Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(s, t));
        Graphs.addEdge(graph, s, t, -10.0);

        GraphPath<String, DefaultWeightedEdge> path
                = new BidirectionalAStarShortestPath<>(graph, (sourceVertex, targetVertex) -> {
            if (sourceVertex.equals(s) && targetVertex.equals(t)) {
                return -10;
            }
            return 0;
        }).getPath(s, t);
        assertNotNull(path);
        assertEquals(-10, (int) path.getWeight());
        assertEquals(1, path.getEdgeList().size());
        assertEquals(1, path.getLength());
    }

    @Test
    public void testGetPath() {
        Graph<String, DefaultWeightedEdge> graph = getSimpleGraph();
        AStarAdmissibleHeuristic<String> heuristic = getSimpleGraphHeuristic();

        assertEquals(Arrays.asList(s, y, z), new BidirectionalAStarShortestPath<>(graph, heuristic).getPath(s, z).getVertexList());
    }

    private Graph<String, DefaultWeightedEdge> getSimpleGraph() {
        Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList(s, t, y, x, z));

        Graphs.addEdge(graph, s, t, 10);
        Graphs.addEdge(graph, s, y, 5);

        Graphs.addEdge(graph, t, y, 2);
        Graphs.addEdge(graph, t, x, 1);

        Graphs.addEdge(graph, y, t, 3);
        Graphs.addEdge(graph, y, z, 2);
        Graphs.addEdge(graph, y, x, 9);

        Graphs.addEdge(graph, x, z, 4);

        Graphs.addEdge(graph, z, x, 6);
        Graphs.addEdge(graph, z, s, 7);

        return graph;
    }

    private AStarAdmissibleHeuristic<String> getSimpleGraphHeuristic() {
        return (sourceVertex, targetVertex) -> {
            if (sourceVertex.equals(s) && targetVertex.equals(z)) {
                return 7;
            } else if (sourceVertex.equals(y) && targetVertex.equals(z)) {
                return 2;
            } else if (sourceVertex.equals(t) && targetVertex.equals(z)) {
                return 4;
            } else if (sourceVertex.equals(x) && targetVertex.equals(z)) {
                return 4;
            } else if (sourceVertex.equals(t) && targetVertex.equals(s)) {
                return 8;
            } else if (sourceVertex.equals(y) && targetVertex.equals(s)) {
                return 5;
            } else if (sourceVertex.equals(x) && targetVertex.equals(s)) {
                return 11;
            } else if (sourceVertex.equals(z) && targetVertex.equals(s)) {
                return 7;
            } else {
                return 0;
            }
        };
    }

    /**
     * Test on a graph with a path from the source node to the target node.
     */
    @Test
    public void testLabyrinth1() {
        this.readLabyrinth(labyrinth1);
        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> shortestPath1 = new BidirectionalAStarShortestPath<>(graph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path = shortestPath1.getPath(sourceNode, targetNode);
        assertNotNull(path);
        assertEquals(47, (int) path.getWeight());
        assertEquals(47, path.getEdgeList().size());
        assertEquals(48, path.getLength() + 1);

        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> shortestPath2 = new BidirectionalAStarShortestPath<>(graph, new EuclideanDistance());
        GraphPath<Node, DefaultWeightedEdge> path2 = shortestPath2.getPath(sourceNode, targetNode);
        assertNotNull(path2);
        assertEquals(47, (int) path2.getWeight());
        assertEquals(47, path2.getEdgeList().size());
    }

    /**
     * Test on a graph where there is no path from the source node to the target node.
     */
    @Test
    public void testLabyrinth2() {
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
    public void testMultiGraph() {
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
    public void testInconsistentHeuristic() {
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

        AStarAdmissibleHeuristic<Integer> h = (s, t) -> {
            if (s == 0 && t == 1) {
                // actual = 0.5822723681370429
                return 0.5822723681370429;
            }
            if (s == 3 && t == 1) {
                // actual = 0.8109462023168071
                return 0.8109462023168071;
            }
            if (s == 3 && t == 2) {
                // actual = 0.9641320715228003
                return 0.9639222864568235;
            }
            if (s == 0 && t == 2) {
                // actual = 0.7354582373430361
                return 0.7354582373430361;
            }

            // all other zero
            return 0d;
        };
        BidirectionalAStarShortestPath<Integer, DefaultWeightedEdge> shortestPath = new BidirectionalAStarShortestPath<>(g, h);

        // shortest path from 3 to 2 is 3->0->1->2 with weight 0.9641320715228003
        assertEquals(0.9641320715228003, shortestPath.getPath(3, 2).getWeight(), 1e-9);
    }

    private class ManhattanDistance
            implements
            AStarAdmissibleHeuristic<Node> {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex) {
            return Math.abs(sourceVertex.x - targetVertex.x)
                    + Math.abs(sourceVertex.y - targetVertex.y);
        }
    }

    private class EuclideanDistance
            implements
            AStarAdmissibleHeuristic<Node> {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex) {
            return Math.sqrt(
                    Math.pow(sourceVertex.x - targetVertex.x, 2)
                            + Math.pow(sourceVertex.y - targetVertex.y, 2));
        }
    }

    private class Node {
        final int x;
        final int y;

        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

}