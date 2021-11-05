/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalGraphColoring}
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphColoringTest
{
    /**
     * Tests coloring of an empty graph
     */
    @Test
    public void testGetColoring1()
    {
        int[][] edges = {};
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        VertexColoringAlgorithm.Coloring<Integer> coloring =
            new ChordalGraphColoring<>(graph).getColoring();
        assertNotNull(coloring);
        assertEquals(0, coloring.getNumberColors());
        assertEquals(0, coloring.getColors().size());
        assertEquals(0, coloring.getColorClasses().size());
    }

    /**
     * Tests coloring on a small clique
     */
    @Test
    public void testGetColoring2()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        VertexColoringAlgorithm.Coloring<Integer> coloring =
            new ChordalGraphColoring<>(graph).getColoring();
        assertNotNull(coloring);
        assertEquals(3, coloring.getNumberColors());
        assertIsColoring(graph, coloring);
    }

    /**
     * Tests coloring on a non-chordal graph.
     */
    @Test
    public void testGetColoring3()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        VertexColoringAlgorithm.Coloring<Integer> coloring =
            new ChordalGraphColoring<>(graph).getColoring();
        assertNull(coloring);
    }

    /**
     * Tests coloring of the big graph
     */
    @Test
    public void testGetColoring4()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 7, 8 },
            { 8, 9 }, { 9, 10 }, { 10, 1 }, { 2, 4 }, { 4, 6 }, { 6, 8 }, { 8, 10 }, { 10, 2 },
            { 2, 6 }, { 2, 8 }, { 4, 8 }, { 4, 10 }, { 6, 10 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        VertexColoringAlgorithm.Coloring<Integer> coloring =
            new ChordalGraphColoring<>(graph).getColoring();
        assertNotNull(coloring);
        assertIsColoring(graph, coloring);
        assertEquals(5, coloring.getNumberColors());
    }

    /**
     * Tests coloring of a pseudograph
     */
    @Test
    public void testGetColoring5()
    {
        int[][] edges = { { 1, 1 }, { 2, 2 }, { 2, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, { 3, 4 },
            { 3, 4 }, { 4, 4 }, { 4, 4 }, { 5, 5 }, { 5, 5 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createPseudograph(edges);

        VertexColoringAlgorithm.Coloring<Integer> coloring =
            new ChordalGraphColoring<>(graph).getColoring();
        assertNotNull(coloring);
        assertIsColoring(graph, coloring);
        assertEquals(3, coloring.getNumberColors());
    }

    /**
     * Checks whether the {@code coloring} is a valid vertex coloring.
     *
     * @param graph the tested graph.
     * @param coloring the tested coloring.
     * @param <V> the graph vertex type.
     * @param <E> the graph edge type.
     */
    private <V,
        E> void assertIsColoring(Graph<V, E> graph, VertexColoringAlgorithm.Coloring<V> coloring)
    {
        Map<V, Integer> colors = coloring.getColors();
        for (V vertex : graph.vertexSet()) {
            for (E edge : graph.edgesOf(vertex)) {
                V opposite = Graphs.getOppositeVertex(graph, edge, vertex);
                if (!vertex.equals(opposite)) {
                    assertNotEquals(colors.get(vertex), colors.get(opposite));
                }
            }
        }
    }
}
