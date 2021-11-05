/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ComplementGraphGenerator
 *
 * @author Joris Kinable
 */
public class ComplementGraphGeneratorTest
{

    @Test
    public void testEmptyGraph()
    {
        // Complement of a graph without edges is the complete graph
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));

        ComplementGraphGenerator<Integer, DefaultEdge> cgg = new ComplementGraphGenerator<>(g);
        Graph<Integer, DefaultEdge> target = new SimpleWeightedGraph<>(DefaultEdge.class);
        cgg.generateGraph(target);

        assertTrue(GraphTests.isComplete(target));

        // complement of a complement graph is the original graph
        ComplementGraphGenerator<Integer, DefaultEdge> cgg2 =
            new ComplementGraphGenerator<>(target);
        Graph<Integer, DefaultEdge> target2 = new SimpleWeightedGraph<>(DefaultEdge.class);
        cgg2.generateGraph(target2);

        assertTrue(target2.edgeSet().isEmpty());
        assertTrue(target2.vertexSet().equals(g.vertexSet()));
    }

    @Test
    public void testUndirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);

        ComplementGraphGenerator<Integer, DefaultEdge> cgg = new ComplementGraphGenerator<>(g);
        Graph<Integer, DefaultEdge> target = new SimpleWeightedGraph<>(DefaultEdge.class);
        cgg.generateGraph(target);

        assertTrue(target.vertexSet().equals(Set.of(0, 1, 2, 3)));
        assertEquals(3, target.edgeSet().size());
        assertTrue(target.containsEdge(0, 3));
        assertTrue(target.containsEdge(2, 3));
        assertTrue(target.containsEdge(1, 3));
    }

    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2));
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);

        ComplementGraphGenerator<Integer, DefaultEdge> cgg = new ComplementGraphGenerator<>(g);
        Graph<Integer, DefaultEdge> target = new SimpleWeightedGraph<>(DefaultEdge.class);
        cgg.generateGraph(target);

        assertTrue(target.vertexSet().equals(Set.of(0, 1, 2)));
        assertEquals(3, target.edgeSet().size());
        assertTrue(target.containsEdge(1, 0));
        assertTrue(target.containsEdge(2, 1));
        assertTrue(target.containsEdge(2, 0));
    }

    @Test
    public void testGraphWithSelfLoops()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2));
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);

        ComplementGraphGenerator<Integer, DefaultEdge> cgg =
            new ComplementGraphGenerator<>(g, true);
        Graph<Integer, DefaultEdge> target = new Pseudograph<>(DefaultEdge.class);
        cgg.generateGraph(target);
        assertTrue(target.vertexSet().equals(Set.of(0, 1, 2)));
        assertEquals(3, target.edgeSet().size());
        for (Integer v : target.vertexSet())
            assertTrue(target.containsEdge(v, v));

    }
}
