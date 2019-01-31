/*
 * (C) Copyright 2010-2018, by France Telecom and Contributors.
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

import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.assertTrue;

public class KSPDiscardsValidPathsTest
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * Example with a biconnected graph but not 3-connected. With a graph not 3-connected, the start
     * vertex and the end vertex can be disconnected by 2 paths.
     */
    @Test
    public void testNot3connectedGraph()
    {
        WeightedMultigraph<String, DefaultWeightedEdge> graph;
        KShortestSimplePaths<String, DefaultWeightedEdge> paths;

        graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        graph.addVertex("S");
        graph.addVertex("T");
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");
        graph.addVertex("H");
        graph.addVertex("I");
        graph.addVertex("J");
        graph.addVertex("K");
        graph.addVertex("L");

        this.addGraphEdge(graph, "S", "A", 1.0);
        this.addGraphEdge(graph, "A", "T", 1.0);
        this.addGraphEdge(graph, "A", "B", 1.0);
        this.addGraphEdge(graph, "B", "T", 1.0);
        this.addGraphEdge(graph, "B", "C", 1.0);

        this.addGraphEdge(graph, "C", "D", 1.0);
        this.addGraphEdge(graph, "C", "E", 1.0);
        this.addGraphEdge(graph, "C", "F", 1.0);
        this.addGraphEdge(graph, "D", "G", 1.0);
        this.addGraphEdge(graph, "E", "G", 1.0);
        this.addGraphEdge(graph, "F", "G", 1.0);

        this.addGraphEdge(graph, "G", "H", 1.0);
        this.addGraphEdge(graph, "H", "I", 1.0);
        this.addGraphEdge(graph, "I", "J", 1.0);
        this.addGraphEdge(graph, "J", "K", 1.0);
        this.addGraphEdge(graph, "K", "L", 1.0);
        this.addGraphEdge(graph, "L", "S", 1.0);

        paths = new KShortestSimplePaths<>(graph);

        assertTrue(paths.getPaths("S", "T", 3).size() == 3);
    }

    /**
     * JUnit test for the bug reported by Bruno Maoili. Example with a connected graph but not
     * 2-connected. With a graph not 2-connected, the start vertex and the end vertex can be
     * disconnected by 1 path.
     */
    @Test
    public void testBrunoMaoili()
    {
        WeightedMultigraph<String, DefaultWeightedEdge> graph;
        KShortestSimplePaths<String, DefaultWeightedEdge> paths;

        graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        this.addGraphEdge(graph, "A", "B", 1.0);
        this.addGraphEdge(graph, "A", "C", 2.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "D", 1.0);
        this.addGraphEdge(graph, "B", "E", 1.0);
        this.addGraphEdge(graph, "C", "D", 1.0);

        paths = new KShortestSimplePaths<>(graph);
        assertTrue(paths.getPaths("A", "E", 2).size() == 2);

        paths = new KShortestSimplePaths<>(graph);
        assertTrue(paths.getPaths("A", "E", 3).size() == 3);

        paths = new KShortestSimplePaths<>(graph);
        assertTrue(paths.getPaths("A", "E", 4).size() == 4);
    }

    private void addGraphEdge(
        WeightedMultigraph<String, DefaultWeightedEdge> graph, String sourceVertex,
        String targetVertex, double weight)
    {
        DefaultWeightedEdge edge = new DefaultWeightedEdge();

        graph.addEdge(sourceVertex, targetVertex, edge);
        graph.setEdgeWeight(edge, weight);
    }
}
