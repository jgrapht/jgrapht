/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ClusteringCoefficient}
 *
 * @author Alexandru Valeanu
 */
public class ClusteringCoefficientTest
{

    @Test
    public void testUndirectedClusteringCoefficient()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        for (int i = 1; i <= 8; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(4, 6);
        graph.addEdge(5, 7);
        graph.addEdge(6, 7);
        graph.addEdge(2, 8);
        graph.addEdge(8, 5);

        ClusteringCoefficient<Integer, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(1, clusteringCoefficient.getVertexScore(1), 0.0);
        assertEquals(0.333333333, clusteringCoefficient.getVertexScore(2), 0.0001);
        assertEquals(0.666666666, clusteringCoefficient.getVertexScore(3), 0.0001);
        assertEquals(0.166666666, clusteringCoefficient.getVertexScore(4), 0.0001);
        assertEquals(0, clusteringCoefficient.getVertexScore(5), 0.0);
        assertEquals(0, clusteringCoefficient.getVertexScore(6), 0.0);
        assertEquals(0, clusteringCoefficient.getVertexScore(7), 0.0);
        assertEquals(0, clusteringCoefficient.getVertexScore(8), 0.0);
    }

    @Test
    public void testUndirected2ClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("A", "D");
        graph.addEdge("B", "C");

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(1.0 / 3.0, clusteringCoefficient.getVertexScore("A"), 0.001);
    }

    @Test
    public void testOneNodeClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex("A");

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(0, clusteringCoefficient.getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testTwoConectedNodesClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex("A");
        graph.addVertex("B");

        graph.addEdge("A", "B");

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(0, clusteringCoefficient.getAverageClusteringCoefficient(), 0.0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullGraphClusteringCoefficient()
    {
        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(null);
    }

    @Test
    public void testCompleteGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CompleteGraphGenerator<String, DefaultEdge> completeGraphGenerator =
            new CompleteGraphGenerator<>(100);

        completeGraphGenerator.generateGraph(graph);

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(1, clusteringCoefficient.getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testStarGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(
            SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        StarGraphGenerator<String, DefaultEdge> starGraphGenerator = new StarGraphGenerator<>(100);

        starGraphGenerator.generateGraph(graph);

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(0, clusteringCoefficient.getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testTriangleDirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";

        directedGraph.addVertex(node1);
        directedGraph.addVertex(node2);
        directedGraph.addVertex(node3);

        directedGraph.addEdge(node1, node2);
        directedGraph.addEdge(node2, node1);
        directedGraph.addEdge(node2, node3);
        directedGraph.addEdge(node3, node2);
        directedGraph.addEdge(node3, node1);
        directedGraph.addEdge(node1, node3);

        assertEquals(
            1, new ClusteringCoefficient<>(directedGraph).getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testSpecial1DirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";
        String node4 = "3";

        directedGraph.addVertex(node1);
        directedGraph.addVertex(node2);
        directedGraph.addVertex(node3);
        directedGraph.addVertex(node4);

        directedGraph.addEdge(node1, node2);
        directedGraph.addEdge(node2, node3);
        directedGraph.addEdge(node2, node4);
        directedGraph.addEdge(node3, node1);
        directedGraph.addEdge(node3, node4);
        directedGraph.addEdge(node4, node1);

        assertEquals(
            0.5, new ClusteringCoefficient<>(directedGraph).getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testSpecial2DirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";
        String node4 = "3";

        directedGraph.addVertex(node1);
        directedGraph.addVertex(node2);
        directedGraph.addVertex(node3);
        directedGraph.addVertex(node4);

        directedGraph.addEdge(node2, node1);
        directedGraph.addEdge(node2, node4);
        directedGraph.addEdge(node3, node1);
        directedGraph.addEdge(node3, node2);
        directedGraph.addEdge(node4, node3);

        assertEquals(
            0.4167, new ClusteringCoefficient<>(directedGraph).getAverageClusteringCoefficient(),
            0.01);
    }

    @Test
    public void testTriangleNonCompleteDirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> directedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";

        directedGraph.addVertex(node1);
        directedGraph.addVertex(node2);
        directedGraph.addVertex(node3);

        directedGraph.addEdge(node1, node2);
        directedGraph.addEdge(node2, node1);
        directedGraph.addEdge(node2, node3);
        directedGraph.addEdge(node3, node2);
        directedGraph.addEdge(node1, node3);

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(directedGraph);

        assertEquals(0.833, clusteringCoefficient.getAverageClusteringCoefficient(), 0.01);
    }

    @Test
    public void testTriangleGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";

        graph.addVertex(node1);
        graph.addVertex(node2);
        graph.addVertex(node3);

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node1);

        assertEquals(1, new ClusteringCoefficient<>(graph).getAverageClusteringCoefficient(), 0.0);
    }

    @Test
    public void testSpecial1UndirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";
        String node4 = "3";
        String node5 = "4";
        String node6 = "5";
        String node7 = "6";

        graph.addVertex(node1);
        graph.addVertex(node2);
        graph.addVertex(node3);
        graph.addVertex(node4);
        graph.addVertex(node5);
        graph.addVertex(node6);
        graph.addVertex(node7);

        graph.addEdge(node1, node2);
        graph.addEdge(node1, node3);
        graph.addEdge(node1, node4);
        graph.addEdge(node1, node5);
        graph.addEdge(node1, node6);
        graph.addEdge(node1, node7);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node6);
        graph.addEdge(node6, node7);
        graph.addEdge(node7, node2);

        ClusteringCoefficient<String, DefaultEdge> clusteringCoefficient =
            new ClusteringCoefficient<>(graph);

        assertEquals(0.4, clusteringCoefficient.getVertexScore(node1), 0.0);
        assertEquals(0.667, clusteringCoefficient.getVertexScore(node3), 0.001);
    }

    @Test
    public void testSpecial2UndirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";
        String node4 = "3";
        String node5 = "4";
        String node6 = "5";
        String node7 = "6";

        graph.addVertex(node1);
        graph.addVertex(node2);
        graph.addVertex(node3);
        graph.addVertex(node4);
        graph.addVertex(node5);
        graph.addVertex(node6);
        graph.addVertex(node7);

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node1);
        graph.addEdge(node1, node4);
        graph.addEdge(node4, node5);
        graph.addEdge(node5, node1);
        graph.addEdge(node1, node6);
        graph.addEdge(node6, node7);
        graph.addEdge(node7, node1);

        assertEquals(
            0.8857, new ClusteringCoefficient<>(graph).getAverageClusteringCoefficient(), 0.01);
    }

    @Test
    public void testSpecial3UndirectedGraphClusteringCoefficient()
    {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String node1 = "0";
        String node2 = "1";
        String node3 = "2";
        String node4 = "3";
        String node5 = "4";
        String node6 = "5";

        graph.addVertex(node1);
        graph.addVertex(node2);
        graph.addVertex(node3);
        graph.addVertex(node4);
        graph.addVertex(node5);
        graph.addVertex(node6);

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node3, node1);
        graph.addEdge(node1, node4);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node6);

        assertEquals(0.333, new ClusteringCoefficient<>(graph).getVertexScore(node1), 0.01);
    }
}
