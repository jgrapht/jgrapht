/*
 * (C) Copyright 2018-2018, by Nikhil Sharma and Contributors.
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
package org.jgrapht.alg.transform;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for LineGraphConverter
 *
 * @author Nikhil Sharma
 * @author Joris Kinable
 */
public class LineGraphConverterTest
{

    @Test
    public void testEmptyGraph()
    {
        // Line Graph of an empty graph should be empty
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));

        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConverter<>(g);
        lgc.convertToLineGraph(new SimpleWeightedGraph<>(DefaultEdge.class));

        assertTrue(GraphTests.isEmpty(g));
    }

    @Test
    public void testStarGraph()
    {
        // Line Graph of a star graph is a complete graph
        Graph<Integer, DefaultEdge> starGraph = new SimpleGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new StarGraphGenerator<>(5);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(starGraph, resultMap);

        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc =
            new LineGraphConverter<>(starGraph);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleGraph<>(DefaultEdge.class);
        lgc.convertToLineGraph(target);

        assertTrue(GraphTests.isComplete(target));
    }

    @Test
    public void testUndirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e25 = g.addEdge(2, 5);
        DefaultEdge e54 = g.addEdge(5, 4);
        DefaultEdge e41 = g.addEdge(4, 1);
        DefaultEdge e43 = g.addEdge(4, 3);
        DefaultEdge e13 = g.addEdge(1, 3);

        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConverter<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleGraph<>(DefaultEdge.class);
        lgc.convertToLineGraph(target);

        assertEquals(target.vertexSet(), g.edgeSet());
        assertEquals(9, target.edgeSet().size());

        assertTrue(target.containsEdge(e12, e25));
        assertTrue(target.containsEdge(e25, e54));
        assertTrue(target.containsEdge(e54, e43));
        assertTrue(target.containsEdge(e43, e13));
        assertTrue(target.containsEdge(e12, e13));
        assertTrue(target.containsEdge(e41, e12));
        assertTrue(target.containsEdge(e41, e54));
        assertTrue(target.containsEdge(e41, e43));
        assertTrue(target.containsEdge(e41, e13));
    }

    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e14 = g.addEdge(1, 4);
        DefaultEdge e23 = g.addEdge(2, 3);
        DefaultEdge e31 = g.addEdge(3, 1);
        DefaultEdge e32 = g.addEdge(3, 2);
        DefaultEdge e34 = g.addEdge(3, 4);
        DefaultEdge e43 = g.addEdge(4, 3);

        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConverter<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleDirectedGraph<>(DefaultEdge.class);
        lgc.convertToLineGraph(target);

        assertEquals(target.vertexSet(), g.edgeSet());
        assertEquals(12, target.edgeSet().size());

        assertTrue(target.containsEdge(e12, e23));
        assertTrue(target.containsEdge(e23, e32));
        assertTrue(target.containsEdge(e23, e34));
        assertTrue(target.containsEdge(e23, e31));
        assertTrue(target.containsEdge(e32, e23));
        assertTrue(target.containsEdge(e31, e12));
        assertTrue(target.containsEdge(e31, e14));
        assertTrue(target.containsEdge(e34, e43));
        assertTrue(target.containsEdge(e43, e34));
        assertTrue(target.containsEdge(e43, e32));
        assertTrue(target.containsEdge(e43, e31));
        assertTrue(target.containsEdge(e14, e43));
    }

    @Test
    public void selfLoopTestUndirected()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e23 = g.addEdge(2, 3);
        DefaultEdge e31 = g.addEdge(3, 1);
        DefaultEdge e22 = g.addEdge(2, 2);
        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConverter<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleGraph<>(DefaultEdge.class);
        lgc.convertToLineGraph(target);

        assertEquals(target.vertexSet(), g.edgeSet());
        assertEquals(5, target.edgeSet().size());

        assertTrue(target.containsEdge(e12, e23));
        assertTrue(target.containsEdge(e12, e31));
        assertTrue(target.containsEdge(e23, e31));
        assertTrue(target.containsEdge(e12, e22));
        assertTrue(target.containsEdge(e22, e23));

    }

    @Test
    public void selfLoopTestDirected()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3));
        DefaultEdge e12 = g.addEdge(1, 2);
        DefaultEdge e23 = g.addEdge(2, 3);
        DefaultEdge e31 = g.addEdge(3, 1);
        DefaultEdge e22 = g.addEdge(2, 2);
        LineGraphConverter<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConverter<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new DirectedPseudograph<>(DefaultEdge.class);
        lgc.convertToLineGraph(target);

        assertEquals(target.vertexSet(), g.edgeSet());
        assertEquals(6, target.edgeSet().size());

        assertTrue(target.containsEdge(e12, e23));
        assertTrue(target.containsEdge(e23, e31));
        assertTrue(target.containsEdge(e31, e12));

        assertTrue(target.containsEdge(e22, e22));
        assertTrue(target.containsEdge(e12, e22));
        assertTrue(target.containsEdge(e22, e23));
    }
}
