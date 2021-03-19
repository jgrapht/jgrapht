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
package org.jgrapht.alg.independentset;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalGraphIndependentSetFinder}
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphIndependentSetFinderTest
{

    /**
     * Tests finding of maximum independent set of an empty graph
     */
    @Test
    public void testGetMaximumIndependentSet1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(0, set.size());
    }

    /**
     * Tests finding of maximum independent set on a clique.
     */
    @Test
    public void testGetMaximumIndependentSet2()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(1, set.size());
    }

    /**
     * Tests finding of a maximum independent set on a non-chordal graph
     */
    @Test
    public void testGetMaximumIndependentSet3()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNull(set);
    }

    /**
     * Tests finding of a maximum independent set on a pseudograph
     */
    @Test
    public void testGetMaximumIndependentSet4()
    {
        int[][] edges = { { 1, 1 }, { 1, 2 }, { 1, 2 }, { 2, 3 }, { 2, 3 }, { 1, 3 }, { 3, 3 },
            { 3, 4 }, { 3, 4 }, { 4, 4 }, { 4, 4 }, { 4, 5 }, { 4, 5 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createPseudograph(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        Set<Integer> set = new ChordalGraphIndependentSetFinder<>(graph).getIndependentSet();
        assertNotNull(set);
        assertEquals(2, set.size());
        assertIsIndependentSet(graph, set);
    }

    /**
     * Checks whether every two vertices from {@code set} aren't adjacent.
     *
     * @param graph the tested graph.
     * @param set the tested set of vertices.
     * @param <V> the graph vertex type.
     * @param <E> the graph edge type.
     */
    private <V, E> void assertIsIndependentSet(Graph<V, E> graph, Set<V> set)
    {
        ArrayList<V> vertices = new ArrayList<>(set);
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < i; j++) {
                assertFalse(graph.containsEdge(vertices.get(i), vertices.get(j)));
            }
        }
    }
}
