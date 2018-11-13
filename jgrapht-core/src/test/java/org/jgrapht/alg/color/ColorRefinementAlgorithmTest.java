/*
 * (C) Copyright 2018-2018, by Oliver Feith and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for the color-refinement algorithm.
 * 
 * @author Oliver Feith
 */
public class ColorRefinementAlgorithmTest
{

    @Test
    public void testTree()
    {
        Graph<Integer, DefaultEdge> tree = new SimpleGraph<>(DefaultEdge.class);

        // Tree has the form ._._|_._.

        Graphs.addAllVertices(tree, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

        tree.addEdge(1, 2);
        tree.addEdge(2, 3);
        tree.addEdge(3, 4);
        tree.addEdge(4, 5);
        tree.addEdge(3, 6);
        tree.addEdge(6, 7);
        tree.addEdge(6, 8);

        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(tree);
        Map<Integer, Integer> colors = CR.getColoring().getColors();

        // symmetric pairs around 3 should have the same color and different colors otherwise

        assertEquals(colors.get(1).intValue(), colors.get(5).intValue());
        assertNotEquals(colors.get(1).intValue(), colors.get(7).intValue());
        assertEquals(colors.get(2).intValue(), colors.get(4).intValue());
        assertEquals(colors.get(7).intValue(), colors.get(8).intValue());
        assertNotEquals(colors.get(1).intValue(), colors.get(2).intValue());
        assertNotEquals(colors.get(2).intValue(), colors.get(3).intValue());
        assertNotEquals(colors.get(3).intValue(), colors.get(6).intValue());
    }

    @Test
    public void testRegular()
    {
        Graph<Integer, DefaultEdge> regularGraph = new SimpleGraph<>(DefaultEdge.class);

        // Graph should be the disjoint union of 2 triangles

        Graphs.addAllVertices(regularGraph, Arrays.asList(1, 2, 3, 4, 5, 6));

        regularGraph.addEdge(1, 2);
        regularGraph.addEdge(2, 3);
        regularGraph.addEdge(3, 1);

        regularGraph.addEdge(4, 5);
        regularGraph.addEdge(5, 6);
        regularGraph.addEdge(6, 4);

        ColorRefinementAlgorithm<Integer, DefaultEdge> CR =
            new ColorRefinementAlgorithm<>(regularGraph);
        Map<Integer, Integer> colors = CR.getColoring().getColors();

        // all vertices should have the same color

        assertEquals(colors.get(1).intValue(), colors.get(2).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(3).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(4).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(5).intValue());
        assertEquals(colors.get(1).intValue(), colors.get(6).intValue());
    }

    @Test
    public void testGraph1()
    {
        Graph<Integer, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph1, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

        graph1.addEdge(1, 2);

        graph1.addEdge(2, 3);
        graph1.addEdge(2, 4);
        graph1.addEdge(2, 6);
        graph1.addEdge(2, 11);

        graph1.addEdge(3, 4);

        graph1.addEdge(4, 6);

        graph1.addEdge(5, 6);

        graph1.addEdge(6, 7);

        graph1.addEdge(7, 8);

        graph1.addEdge(8, 9);
        graph1.addEdge(8, 10);
        graph1.addEdge(8, 11);

        graph1.addEdge(9, 10);
        graph1.addEdge(9, 11);

        graph1.addEdge(10, 11);

        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(graph1);
        Map<Integer, Integer> colors = CR.getColoring().getColors();

        // 9 and 10 should have the same color, all others should have distinct colors

        for (int i = 1; i < 11; i++) {
            for (int j = i + 1; j <= 11; j++) {
                if (i != 9 || j != 10) {
                    assertNotEquals(colors.get(i).intValue(), colors.get(j).intValue());
                }
            }
        }
        assertEquals(colors.get(9).intValue(), colors.get(10).intValue());
    }

    @Test
    public void testDirectedGraph1()
    {
        Graph<Integer, DefaultEdge> graph1 = new DefaultDirectedGraph<>(DefaultEdge.class);

        Graphs.addAllVertices(graph1, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

        graph1.addEdge(1, 2);

        graph1.addEdge(2, 4);

        graph1.addEdge(3, 2);

        graph1.addEdge(4, 2);
        graph1.addEdge(4, 5);
        graph1.addEdge(4, 6);

        graph1.addEdge(5, 8);

        graph1.addEdge(6, 7);

        graph1.addEdge(7, 8);

        graph1.addEdge(8, 4);
        graph1.addEdge(8, 6);

        ColorRefinementAlgorithm<Integer, DefaultEdge> CR = new ColorRefinementAlgorithm<>(graph1);
        Map<Integer, Integer> colors = CR.getColoring().getColors();

        // 1 and 3 should have the same color, all others should have distinct colors

        for (int i = 1; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                if ((i == 1 && j == 3) || (i == 5 && j == 7)) {
                    assertEquals(colors.get(i).intValue(), colors.get(j).intValue());
                } else {
                    assertNotEquals(colors.get(i).intValue(), colors.get(j).intValue());
                }
            }
        }
        assertEquals(colors.get(1).intValue(), colors.get(3).intValue());
    }
}
