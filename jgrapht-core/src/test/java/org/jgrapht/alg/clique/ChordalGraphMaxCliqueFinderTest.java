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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalGraphMaxCliqueFinder}
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphMaxCliqueFinderTest
{
    /**
     * Tests maximum clique finding on an empty graph.
     */
    @Test
    public void testGetMaximumClique1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        Set<Integer> clique = new ChordalGraphMaxCliqueFinder<>(graph).getClique();
        assertNotNull(clique);
        assertEquals(0, clique.size());
    }

    /**
     * Tests maximum clique finding on a chordal graph
     */
    @Test
    public void testGetMaximumClique2()
    {
        int[][] edges = { { 1, 2 }, { 3, 4 }, { 3, 5 }, { 3, 6 }, { 4, 5 }, { 4, 6 }, { 5, 6 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        Set<Integer> clique = new ChordalGraphMaxCliqueFinder<>(graph).getClique();
        assertNotNull(clique);
        assertEquals(4, clique.size());
        assertIsClique(graph, clique);
    }

    /**
     * Tests maximum clique finding on a non-chordal graph
     */
    @Test
    public void testGetMaximumClique3()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        Set<Integer> clique = new ChordalGraphMaxCliqueFinder<>(graph).getClique();
        assertNull(clique);
    }

    /**
     * Tests maximum clique finding on a pseudograph
     */
    @Test
    public void testGetMaximumClique4()
    {
        int[][] edges = { { 1, 1 }, { 1, 1 }, { 1, 2 }, { 1, 2 }, { 1, 2 }, { 2, 2 }, { 2, 2 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createPseudograph(edges);

        Set<Integer> clique = new ChordalGraphMaxCliqueFinder<>(graph).getClique();
        assertNotNull(clique);
        assertEquals(2, clique.size());
        assertIsClique(graph, clique);
    }

    /**
     * Checks whether every two vertices from {@code set} are adjacent.
     *
     * @param graph the tested graph.
     * @param set the tested set of vertices.
     * @param <V> the graph vertex type.
     * @param <E> the graph edge type.
     */
    private <V, E> void assertIsClique(Graph<V, E> graph, Set<V> set)
    {
        ArrayList<V> vertices = new ArrayList<>(set);
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < i; j++) {
                assertTrue(graph.containsEdge(vertices.get(i), vertices.get(j)));
            }
        }
    }
}
