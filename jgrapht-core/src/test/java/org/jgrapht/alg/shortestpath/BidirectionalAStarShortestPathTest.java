/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test class for {@link BidirectionalAStarShortestPath} class.
 *
 * @author Semen Chudakov
 */
public class BidirectionalAStarShortestPathTest
    extends
    BaseHeuristicSearchTest
{
    private static final String S = "S";
    private static final String T = "T";
    private static final String Y = "Y";
    private static final String X = "X";
    private static final String Z = "Z";

    @Test
    public void testEmptyGraph()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        graph.addVertex(S);
        new BidirectionalAStarShortestPath<>(graph, (sourceVertex, targetVertex) -> 0).getPaths(S);
    }

    @Test
    public void testSimpleGraph()
    {
        Graph<String, DefaultWeightedEdge> graph = getSimpleGraph();
        AStarAdmissibleHeuristic<String> heuristic = getSimpleGraphHeuristic();
        assertEquals(
            Arrays.asList(S, Y, Z),
            new BidirectionalAStarShortestPath<>(graph, heuristic).getPath(S, Z).getVertexList());
    }

    private Graph<String, DefaultWeightedEdge> getSimpleGraph()
    {
        Graph<String, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(graph, Arrays.asList(S, T, Y, X, Z));

        Graphs.addEdge(graph, S, T, 10);
        Graphs.addEdge(graph, S, Y, 5);

        Graphs.addEdge(graph, T, Y, 2);
        Graphs.addEdge(graph, T, X, 1);

        Graphs.addEdge(graph, Y, T, 3);
        Graphs.addEdge(graph, Y, Z, 2);
        Graphs.addEdge(graph, Y, X, 9);

        Graphs.addEdge(graph, X, Z, 4);

        Graphs.addEdge(graph, Z, X, 6);
        Graphs.addEdge(graph, Z, S, 7);

        return graph;
    }

    private AStarAdmissibleHeuristic<String> getSimpleGraphHeuristic()
    {
        return (sourceVertex, targetVertex) -> {
            if (sourceVertex.equals(S) && targetVertex.equals(Z)) {
                return 7;
            } else if (sourceVertex.equals(Y) && targetVertex.equals(Z)) {
                return 2;
            } else if (sourceVertex.equals(T) && targetVertex.equals(Z)) {
                return 4;
            } else if (sourceVertex.equals(X) && targetVertex.equals(Z)) {
                return 4;
            } else if (sourceVertex.equals(T) && targetVertex.equals(S)) {
                return 8;
            } else if (sourceVertex.equals(Y) && targetVertex.equals(S)) {
                return 5;
            } else if (sourceVertex.equals(X) && targetVertex.equals(S)) {
                return 11;
            } else if (sourceVertex.equals(Z) && targetVertex.equals(S)) {
                return 7;
            } else {
                return 0;
            }
        };
    }

    @Test
    public void testLabyrinth1()
    {
        this.readLabyrinth(labyrinth1);
        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> shortestPath1 =
            new BidirectionalAStarShortestPath<>(graph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path = shortestPath1.getPath(sourceNode, targetNode);
        assertNotNull(path);
        assertEquals(47, (int) path.getWeight());
        assertEquals(47, path.getEdgeList().size());
        assertEquals(48, path.getLength() + 1);

        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> shortestPath2 =
            new BidirectionalAStarShortestPath<>(graph, new EuclideanDistance());
        GraphPath<Node, DefaultWeightedEdge> path2 = shortestPath2.getPath(sourceNode, targetNode);
        assertNotNull(path2);
        assertEquals(47, (int) path2.getWeight());
        assertEquals(47, path2.getEdgeList().size());
    }

    @Test
    public void testLabyrinth2()
    {
        this.readLabyrinth(labyrinth2);
        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new BidirectionalAStarShortestPath<>(graph, new ManhattanDistance());
        GraphPath<Node, DefaultWeightedEdge> path =
            aStarShortestPath.getPath(sourceNode, targetNode);
        assertNull(path);
    }

    @Test
    public void testMultiGraph()
    {
        Graph<Node, DefaultWeightedEdge> multigraph = getMultigraph();
        BidirectionalAStarShortestPath<Node, DefaultWeightedEdge> aStarShortestPath =
            new BidirectionalAStarShortestPath<>(multigraph, new ManhattanDistance());
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

        BidirectionalAStarShortestPath<Integer, DefaultWeightedEdge> shortestPath =
            new BidirectionalAStarShortestPath<>(g, h);

        // shortest path from 3 to 2 is 3->0->1->2 with weight 0.9641320715228003
        assertEquals(0.9641320715228003, shortestPath.getPath(3, 2).getWeight(), 1e-9);
    }

    @Test
    public void testRandomGraphs()
    {
        int n = 1000;
        double p = 0.40;
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultWeightedEdge> graph = getGnpRandomGraph(n, p);
            Integer[] vertices = graph.vertexSet().toArray(new Integer[0]);
            Set<Integer> landmarks = new HashSet<>();
            int numOfLandmarks = 5;
            while (landmarks.size() < numOfLandmarks) {
                int position = (int) (Math.random() * graph.vertexSet().size());
                landmarks.add(vertices[position]);
            }
            AStarAdmissibleHeuristic<Integer> heuristic =
                new ALTAdmissibleHeuristic<>(graph, landmarks);
            for (int j = 0; j < 10; j++) {
                int source = (int) (Math.random() * n);
                int target = (int) (Math.random() * n);
                testCorrectness(graph, source, target, heuristic);
            }
        }
    }

    private void testCorrectness(
        Graph<Integer, DefaultWeightedEdge> graph, int source, int target,
        AStarAdmissibleHeuristic<Integer> heuristic)
    {
        DijkstraShortestPath<Integer, DefaultWeightedEdge> dijkstraShortestPath =
            new DijkstraShortestPath<>(graph);
        BidirectionalAStarShortestPath<Integer,
            DefaultWeightedEdge> bidirectionalAStarShortestPath =
                new BidirectionalAStarShortestPath<>(graph, heuristic);
        GraphPath<Integer, DefaultWeightedEdge> path1 =
            dijkstraShortestPath.getPath(source, target);
        GraphPath<Integer, DefaultWeightedEdge> path2 =
            bidirectionalAStarShortestPath.getPath(source, target);
        if (path1 == null) {
            assertNull(path2);
        } else {
            assertEquals(path1.getWeight(), path2.getWeight(), 1e-9);
        }
    }

    private Graph<Integer, DefaultWeightedEdge> getGnpRandomGraph(int n, double p)
    {
        GraphGenerator<Integer, DefaultWeightedEdge, Integer> generator =
            new GnpRandomGraphGenerator<>(n, p);
        DefaultUndirectedWeightedGraph<Integer, DefaultWeightedEdge> result =
            new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        result.setVertexSupplier(SupplierUtil.createIntegerSupplier());
        generator.generateGraph(result);
        for (DefaultWeightedEdge e : result.edgeSet()) {
            result.setEdgeWeight(e, (int) (Math.random() * 10));
        }
        return result;
    }
}
