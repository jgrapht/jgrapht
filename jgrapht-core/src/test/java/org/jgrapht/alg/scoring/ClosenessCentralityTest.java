/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit tests for closeness centrality.
 * 
 * @author Dimitrios Michail
 */
public class ClosenessCentralityTest
{

    @Test
    public void testOutgoing()
    {
        Graph<String, DefaultEdge> g = createInstance1();

        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, false, true);

        assertEquals(4d / 7, pr.getVertexScore("1"), 1e-9);
        assertEquals(4d / 9, pr.getVertexScore("2"), 1e-9);
        assertEquals(4d / 8, pr.getVertexScore("3"), 1e-9);
        assertEquals(4d / 6, pr.getVertexScore("4"), 1e-9);
        assertEquals(4d / 10, pr.getVertexScore("5"), 1e-9);
    }

    @Test
    public void testIncoming()
    {
        Graph<String, DefaultEdge> g = createInstance1();

        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, true, true);

        assertEquals(4d / 9, pr.getVertexScore("1"), 1e-9);
        assertEquals(4d / 10, pr.getVertexScore("2"), 1e-9);
        assertEquals(4d / 5, pr.getVertexScore("3"), 1e-9);
        assertEquals(4d / 7, pr.getVertexScore("4"), 1e-9);
        assertEquals(4d / 9, pr.getVertexScore("5"), 1e-9);
    }

    @Test
    public void testIncomingNoNormalization()
    {
        Graph<String, DefaultEdge> g = createInstance1();

        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, true, false);

        assertEquals(1d / 9, pr.getVertexScore("1"), 1e-9);
        assertEquals(1d / 10, pr.getVertexScore("2"), 1e-9);
        assertEquals(1d / 5, pr.getVertexScore("3"), 1e-9);
        assertEquals(1d / 7, pr.getVertexScore("4"), 1e-9);
        assertEquals(1d / 9, pr.getVertexScore("5"), 1e-9);
    }

    @Test
    public void testUndirected()
    {
        Graph<String, DefaultEdge> g = new AsUndirectedGraph<>(createInstance1());

        VertexScoringAlgorithm<String, Double> pr1 = new ClosenessCentrality<>(g, true, true);
        VertexScoringAlgorithm<String, Double> pr2 = new ClosenessCentrality<>(g, false, true);

        assertEquals(4d / 5, pr1.getVertexScore("1"), 1e-9);
        assertEquals(4d / 5, pr2.getVertexScore("1"), 1e-9);
        assertEquals(4d / 6, pr1.getVertexScore("2"), 1e-9);
        assertEquals(4d / 6, pr2.getVertexScore("2"), 1e-9);
        assertEquals(4d / 4, pr1.getVertexScore("3"), 1e-9);
        assertEquals(4d / 4, pr2.getVertexScore("3"), 1e-9);
        assertEquals(4d / 5, pr1.getVertexScore("4"), 1e-9);
        assertEquals(4d / 5, pr2.getVertexScore("4"), 1e-9);
        assertEquals(4d / 6, pr1.getVertexScore("5"), 1e-9);
        assertEquals(4d / 6, pr2.getVertexScore("5"), 1e-9);
    }

    @Test
    public void testNegativeWeights()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");
        g.addEdge("1", "2");
        DefaultWeightedEdge e13 = g.addEdge("1", "3");
        g.addEdge("2", "3");
        g.addEdge("3", "4");
        g.addEdge("4", "1");
        g.addEdge("4", "5");
        g.addEdge("5", "3");

        g.setEdgeWeight(e13, -1d);

        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, false, true);

        assertEquals(4d / 1, pr.getVertexScore("1"), 1e-9);
        assertEquals(4d / 9, pr.getVertexScore("2"), 1e-9);
        assertEquals(4d / 8, pr.getVertexScore("3"), 1e-9);
        assertEquals(4d / 4, pr.getVertexScore("4"), 1e-9);
        assertEquals(4d / 10, pr.getVertexScore("5"), 1e-9);
    }

    @Test
    public void testDisconnectedOutgoing()
    {
        Graph<String, DefaultEdge> g = createInstance1();
        g.addVertex("6");

        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, false, true);

        assertEquals(0d, pr.getVertexScore("1"), 1e-9);
        assertEquals(0d, pr.getVertexScore("2"), 1e-9);
        assertEquals(0d, pr.getVertexScore("3"), 1e-9);
        assertEquals(0d, pr.getVertexScore("4"), 1e-9);
        assertEquals(0d, pr.getVertexScore("5"), 1e-9);
        assertEquals(0d, pr.getVertexScore("6"), 1e-9);
    }

    @Test
    public void testSingletonWithNormalize()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex("1");
        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, false, true);
        assertEquals(Double.NaN, pr.getVertexScore("1"), 1e-9);
    }

    @Test
    public void testSingletonWithoutNormalize()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex("1");
        VertexScoringAlgorithm<String, Double> pr = new ClosenessCentrality<>(g, false, false);
        assertEquals(Double.POSITIVE_INFINITY, pr.getVertexScore("1"), 1e-9);
    }

    private Graph<String, DefaultEdge> createInstance1()
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");
        g.addVertex("5");
        g.addEdge("1", "2");
        g.addEdge("1", "3");
        g.addEdge("2", "3");
        g.addEdge("3", "4");
        g.addEdge("4", "1");
        g.addEdge("4", "5");
        g.addEdge("5", "3");
        return g;
    }

}
