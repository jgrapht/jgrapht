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

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for AlphaCentrality
 * 
 * @author Dimitrios Michail
 * @author Pratik Tibrewal
 */
public class AlphaCentralityTest
{
    @Test
    public void testGraph2Nodes()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2");
        g.addEdge("2", "1");

        VertexScoringAlgorithm<String, Double> pr = new AlphaCentrality<>(g);

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

        VertexScoringAlgorithm<String, Double> pr = new AlphaCentrality<>(g);

        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("2"), 0.0001);
        assertEquals(pr.getVertexScore("1"), pr.getVertexScore("3"), 0.0001);
    }

    @Test
    public void testGraph1()
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

        g.addEdge("A", "E");
        g.addEdge("A", "F");
        g.addEdge("B", "E");
        g.addEdge("B", "F");
        g.addEdge("C", "E");
        g.addEdge("D", "E");
        g.addEdge("1", "E");
        g.addEdge("1", "F");
        g.addEdge("E", "1");
        g.addEdge("2", "E");
        g.addEdge("2", "F");
        g.addEdge("3", "F");
        g.addEdge("F", "3");
        g.addEdge("4", "F");
        g.addEdge("E", "4");
        g.addEdge("4", "5");
        g.addEdge("E", "F");

        VertexScoringAlgorithm<String, Double> pr = new AlphaCentrality<>(g, 0.85);

        assertEquals(pr.getVertexScore("A"), 1.0000, 0.5);
        assertEquals(pr.getVertexScore("B"), 1.0000, 0.5);
        assertEquals(pr.getVertexScore("C"), 1.0000, 0.5);
        assertEquals(pr.getVertexScore("D"), 1.0000, 0.5);
        assertEquals(pr.getVertexScore("E"), 22.000, 0.5);
        assertEquals(pr.getVertexScore("F"), 204.00, 0.5);
        assertEquals(pr.getVertexScore("1"), 20.000, 0.5);
        assertEquals(pr.getVertexScore("2"), 1.0000, 0.5);
        assertEquals(pr.getVertexScore("3"), 174.00, 0.5);
        assertEquals(pr.getVertexScore("4"), 20.000, 0.5);
        assertEquals(pr.getVertexScore("5"), 18.000, 0.5);
    }

    @Test
    public void testGraph2()
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

        g.addEdge("A", "E");
        g.addEdge("A", "F");
        g.addEdge("B", "E");
        g.addEdge("B", "F");
        g.addEdge("C", "E");
        g.addEdge("D", "E");
        g.addEdge("1", "E");
        g.addEdge("1", "F");
        g.addEdge("E", "1");
        g.addEdge("2", "E");
        g.addEdge("2", "F");
        g.addEdge("3", "F");
        g.addEdge("F", "3");
        g.addEdge("4", "F");
        g.addEdge("E", "4");
        g.addEdge("4", "5");
        g.addEdge("E", "F");

        VertexScoringAlgorithm<String, Double> pr = new AlphaCentrality<>(g, 0.15);

        assertEquals(pr.getVertexScore("A"), 1.0000, 0.05);
        assertEquals(pr.getVertexScore("B"), 1.0000, 0.05);
        assertEquals(pr.getVertexScore("C"), 1.0000, 0.05);
        assertEquals(pr.getVertexScore("D"), 1.0000, 0.05);
        assertEquals(pr.getVertexScore("E"), 1.9400, 0.05);
        assertEquals(pr.getVertexScore("F"), 2.3300, 0.05);
        assertEquals(pr.getVertexScore("1"), 1.2900, 0.05);
        assertEquals(pr.getVertexScore("2"), 1.0000, 0.05);
        assertEquals(pr.getVertexScore("3"), 1.3500, 0.05);
        assertEquals(pr.getVertexScore("4"), 1.2900, 0.05);
        assertEquals(pr.getVertexScore("5"), 1.1900, 0.05);
    }

    @Test
    public void testGraph3()
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

        g.addEdge("A", "E");
        g.addEdge("A", "F");
        g.addEdge("B", "E");
        g.addEdge("B", "F");
        g.addEdge("C", "E");
        g.addEdge("D", "E");
        g.addEdge("1", "E");
        g.addEdge("1", "F");
        g.addEdge("E", "1");
        g.addEdge("2", "E");
        g.addEdge("2", "F");
        g.addEdge("3", "F");
        g.addEdge("F", "3");
        g.addEdge("4", "F");
        g.addEdge("E", "4");
        g.addEdge("4", "5");
        g.addEdge("E", "F");

        Map<String, Double> exogenousfactormap = new HashMap<>();
        for (String v : g.vertexSet()) {
            exogenousfactormap.put(v, 1.0);
        }
        exogenousfactormap.put("4", 2.0);

        ToDoubleFunction<String> exogenousFactorFunction = (v) -> exogenousfactormap.get(v);

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.15, exogenousFactorFunction);

        assertEquals(pr.getVertexScore("A"), 1.0000, 0.005);
        assertEquals(pr.getVertexScore("B"), 1.0000, 0.005);
        assertEquals(pr.getVertexScore("C"), 1.0000, 0.005);
        assertEquals(pr.getVertexScore("D"), 1.0000, 0.005);
        assertEquals(pr.getVertexScore("E"), 1.9400, 0.005);
        assertEquals(pr.getVertexScore("F"), 2.4800, 0.005);
        assertEquals(pr.getVertexScore("1"), 1.2900, 0.005);
        assertEquals(pr.getVertexScore("2"), 1.0000, 0.005);
        assertEquals(pr.getVertexScore("3"), 1.3700, 0.005);
        assertEquals(pr.getVertexScore("4"), 2.2900, 0.005);
        assertEquals(pr.getVertexScore("5"), 1.3400, 0.005);
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

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 1.0000, 0.0001);
        assertEquals(pr.getVertexScore("a"), 64.7500, 0.0001);
        assertEquals(pr.getVertexScore("b"), 18.0000, 0.0001);
        assertEquals(pr.getVertexScore("c"), 5.2500, 0.0001);
    }

    @Test
    public void testweightedGraph2()
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

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 1.0000, 0.0001);
        assertEquals(pr.getVertexScore("a"), 1.8500, 0.0001);
        assertEquals(pr.getVertexScore("b"), 1.8500, 0.0001);
        assertEquals(pr.getVertexScore("c"), 1.8500, 0.0001);
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

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0001);

        assertEquals(pr.getVertexScore("center"), 1.0000, 0.0001);
        assertEquals(pr.getVertexScore("a"), 1.8500, 0.0001);
        assertEquals(pr.getVertexScore("b"), 1.8500, 0.0001);
        assertEquals(pr.getVertexScore("c"), 1.8500, 0.0001);
        assertEquals(pr.getVertexScore("d"), 1.0000, 0.0001);
    }

    @Test
    public void testEmptyGraph()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0001);

        assertTrue(pr.getScores().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
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

        VertexScoringAlgorithm<String, Double> pr =
            new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0001);

        pr.getVertexScore("unknown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadParameters1()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        new AlphaCentrality<>(g, 1.25, 1.0, 100, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadParameters2()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        new AlphaCentrality<>(g, 0.85, 1.0, 0, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadParameters3()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        new AlphaCentrality<>(g, 0.85, 1.0, 100, 0.0);
    }
}
