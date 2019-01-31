/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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

import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit tests for PageRank
 * 
 * @author Dimitrios Michail
 */
public class PageRankTest
{

    @Test
    public void testGraph2Nodes()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2");
        g.addEdge("2", "1");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("2"), 0.0001);
    }

    @Test
    public void testGraph3Nodes()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addEdge("1", "2");
        g.addEdge("2", "3");
        g.addEdge("3", "1");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("2"), 0.0001);
        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("3"), 0.0001);
    }

    @Test
    public void testGraphWikipedia()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");

        g.addEdge("B", "C");
        g.addEdge("C", "B");
        g.addEdge("D", "A");
        g.addEdge("D", "B");
        g.addEdge("E", "D");
        g.addEdge("E", "B");
        g.addEdge("E", "F");
        g.addEdge("F", "B");
        g.addEdge("F", "E");
        g.addEdge("1", "B");
        g.addEdge("1", "E");
        g.addEdge("2", "B");
        g.addEdge("2", "E");
        g.addEdge("3", "B");
        g.addEdge("3", "E");
        g.addEdge("4", "E");
        g.addEdge("5", "E");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("A"), 0.03278, 0.0001);
        assertEquals(pr.getVertexScore("B"), 0.38435, 0.0001);
        assertEquals(pr.getVertexScore("C"), 0.34295, 0.0001);
        assertEquals(pr.getVertexScore("D"), 0.03908, 0.0001);
        assertEquals(pr.getVertexScore("E"), 0.08088, 0.0001);
        assertEquals(pr.getVertexScore("F"), 0.03908, 0.0001);
        assertEquals(pr.getVertexScore("1"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("2"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("3"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("4"), 0.01616, 0.0001);
        assertEquals(pr.getVertexScore("5"), 0.01616, 0.0001);
    }

    @Test
    public void testUndirectedGraphWikipedia()
    {
        Pseudograph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");

        g.addEdge("B", "C");
        g.addEdge("C", "B");
        g.addEdge("D", "A");
        g.addEdge("D", "B");
        g.addEdge("E", "D");
        g.addEdge("E", "B");
        g.addEdge("E", "F");
        g.addEdge("F", "B");
        g.addEdge("F", "E");
        g.addEdge("1", "B");
        g.addEdge("1", "E");
        g.addEdge("2", "B");
        g.addEdge("2", "E");
        g.addEdge("3", "B");
        g.addEdge("3", "E");
        g.addEdge("4", "E");
        g.addEdge("5", "E");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g);

        assertEquals(pr.getVertexScore("A"), 0.0404, 0.0001);
        assertEquals(pr.getVertexScore("B"), 0.2152, 0.0001);
        assertEquals(pr.getVertexScore("C"), 0.0593, 0.0001);
        assertEquals(pr.getVertexScore("D"), 0.0945, 0.0001);
        assertEquals(pr.getVertexScore("E"), 0.2511, 0.0001);
        assertEquals(pr.getVertexScore("F"), 0.0839, 0.0001);
        assertEquals(pr.getVertexScore("1"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("2"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("3"), 0.0602, 0.0001);
        assertEquals(pr.getVertexScore("4"), 0.0373, 0.0001);
        assertEquals(pr.getVertexScore("5"), 0.0373, 0.0001);
    }

    @Test
    public void testWeightedGraph1()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");

        g.setEdgeWeight(g.addEdge("center", "a"), 75.0);
        g.setEdgeWeight(g.addEdge("center", "b"), 20.0);
        g.setEdgeWeight(g.addEdge("center", "c"), 5.0);

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 0.2061, 0.0001);
        assertEquals(pr.getVertexScore("a"), 0.3376, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.2412, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.2149, 0.0001);
    }

    @Test
    public void testUnweightedGraph1()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");

        g.setEdgeWeight(g.addEdge("center", "a"), 1.0);
        g.setEdgeWeight(g.addEdge("center", "b"), 1.0);
        g.setEdgeWeight(g.addEdge("center", "c"), 1.0);

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 0.2061, 0.0001);
        assertEquals(pr.getVertexScore("a"), 0.2646, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.2646, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.2646, 0.0001);

        // for (String v : g.vertexSet()) {
        // System.out.println("pagerank(" + v + ") = " + pr.getVertexScore(v));
        // }
    }

    @Test
    public void testUnweightedGraph2()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("center", "a");
        g.addEdge("center", "b");
        g.addEdge("center", "c");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 0.1709, 0.0001);
        assertEquals(pr.getVertexScore("a"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.21937, 0.0001);
        assertEquals(pr.getVertexScore("d"), 0.1709, 0.0001);

        // for (String v : g.vertexSet()) {
        // System.out.println("pagerank(" + v + ") = " + pr.getVertexScore(v));
        // }
    }

    @Test
    public void testEmptyGraph()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        assertTrue(pr.getScores().isEmpty());
    }

    @Test
    public void testNonExistantVertex()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("center", "a");
        g.addEdge("center", "b");
        g.addEdge("center", "c");

        VertexScoringAlgorithm<String, Double> pr = new PageRank<>(g, 0.85, 100, 0.0001);

        try {
            pr.getVertexScore("unknown");
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testBadParameters()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        try {
            new PageRank<>(g, 1.1, 100, 0.0001);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

        try {
            new PageRank<>(g, 0.85, 0, 0.0001);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

        try {
            new PageRank<>(g, 0.85, 100, 0.0);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }

    }

}
