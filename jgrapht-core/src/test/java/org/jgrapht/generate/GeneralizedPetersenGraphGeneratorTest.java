/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for GeneralizedPetersenGraphGenerator
 *
 * @author Joris Kinable
 */
public class GeneralizedPetersenGraphGeneratorTest
{

    @Test
    public void testCubicalGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GeneralizedPetersenGraphGenerator<Integer, DefaultEdge> gpgg =
            new GeneralizedPetersenGraphGenerator<>(4, 1);
        gpgg.generateGraph(g);
        this.validateBasics(g, 8, 12, 3, 3, 4);
        assertTrue(GraphTests.isBipartite(g));
        assertTrue(GraphTests.isCubic(g));

    }

    @Test
    public void testPetersenGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.petersenGraph();
        this.validateBasics(g, 10, 15, 2, 2, 5);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testDürerGraphGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.dürerGraph();
        this.validateBasics(g, 12, 18, 3, 4, 3);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testDodecahedronGraphGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.dodecahedronGraph();
        this.validateBasics(g, 20, 30, 5, 5, 5);
        assertTrue(GraphTests.isCubic(g));
    }

    @Test
    public void testDesarguesGraphGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.desarguesGraph();
        this.validateBasics(g, 20, 30, 5, 5, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testNauruGraphGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.nauruGraph();
        this.validateBasics(g, 24, 36, 4, 4, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    @Test
    public void testMöbiusKantorGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.möbiusKantorGraph();
        this.validateBasics(g, 16, 24, 4, 4, 6);
        assertTrue(GraphTests.isCubic(g));
        assertTrue(GraphTests.isBipartite(g));
    }

    private <V, E> void validateBasics(
        Graph<V, E> g, int vertices, int edges, int radius, int diameter, int girt)
    {
        assertEquals(vertices, g.vertexSet().size());
        assertEquals(edges, g.edgeSet().size());
        GraphMeasurer<V, E> gm = new GraphMeasurer<>(g);
        assertEquals(radius, gm.getRadius(), 0.00000001);
        assertEquals(diameter, gm.getDiameter(), 0.00000001);
        assertEquals(girt, GraphMetrics.getGirth(g), 0.00000001);
    }
}
