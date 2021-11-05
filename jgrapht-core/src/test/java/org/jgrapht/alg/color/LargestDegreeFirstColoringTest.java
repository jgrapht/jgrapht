/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Coloring tests
 * 
 * @author Dimitrios Michail
 */
public class LargestDegreeFirstColoringTest
    extends
    BaseColoringTest
{

    @Override
    protected VertexColoringAlgorithm<Integer> getAlgorithm(Graph<Integer, DefaultEdge> graph)
    {
        return new LargestDegreeFirstColoring<>(graph);
    }

    @Test
    public void testMyciel3()
    {
        Graph<Integer, DefaultEdge> g = createMyciel3Graph();
        assertColoring(g, getAlgorithm(g).getColoring(), 4);
    }

    @Override
    protected int getExpectedResultOnDSaturNonOptimalGraph()
    {
        return 4;
    }

    @Test
    public void testLargestDegreeFirstColoring()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 5);

        Coloring<Integer> coloring = new LargestDegreeFirstColoring<>(g).getColoring();
        assertEquals(3, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();
        assertEquals(0, colors.get(1).intValue());
        assertEquals(2, colors.get(2).intValue());
        assertEquals(1, colors.get(3).intValue());
        assertEquals(2, colors.get(4).intValue());
        assertEquals(2, colors.get(5).intValue());
    }

    @Test
    public void testLargestDegreeFirstColoring1()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 3);
        g.addEdge(1, 4);
        g.addEdge(1, 5);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        g.addEdge(3, 6);
        g.addEdge(5, 6);

        Coloring<Integer> coloring = new LargestDegreeFirstColoring<>(g).getColoring();
        assertEquals(3, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();
        assertEquals(1, colors.get(1).intValue());
        assertEquals(2, colors.get(2).intValue());
        assertEquals(0, colors.get(3).intValue());
        assertEquals(2, colors.get(4).intValue());
        assertEquals(2, colors.get(5).intValue());
        assertEquals(1, colors.get(6).intValue());
    }

    @Test
    public void testLargestDegreeFirstColoringNonSimple()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(2, 3);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        for (int i = 0; i < 20000; i++) {
            g.addEdge(5, 6);
        }

        Coloring<Integer> coloring = new LargestDegreeFirstColoring<>(g).getColoring();
        assertEquals(3, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();
        assertEquals(0, colors.get(1).intValue());
        assertEquals(0, colors.get(2).intValue());
        assertEquals(1, colors.get(3).intValue());
        assertEquals(2, colors.get(4).intValue());
        assertEquals(0, colors.get(5).intValue());
        assertEquals(1, colors.get(6).intValue());
    }

    @Test
    public void testLargestDegreeFirstColoringSimple()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5, 6));
        g.addEdge(2, 3);
        g.addEdge(4, 5);
        g.addEdge(4, 6);
        g.addEdge(5, 6);
        g.addEdge(5, 3);

        Coloring<Integer> coloring = new LargestDegreeFirstColoring<>(g).getColoring();
        assertEquals(3, coloring.getNumberColors());
        Map<Integer, Integer> colors = coloring.getColors();
        assertEquals(0, colors.get(1).intValue());
        assertEquals(0, colors.get(2).intValue());
        assertEquals(1, colors.get(3).intValue());
        assertEquals(1, colors.get(4).intValue());
        assertEquals(0, colors.get(5).intValue());
        assertEquals(2, colors.get(6).intValue());
    }

}
