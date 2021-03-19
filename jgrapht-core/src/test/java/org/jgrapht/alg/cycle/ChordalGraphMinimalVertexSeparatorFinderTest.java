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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link ChordalGraphMinimalVertexSeparatorFinder}
 *
 * @author Timofey Chudakov
 */
public class ChordalGraphMinimalVertexSeparatorFinderTest
{

    /**
     * Test on empty graph
     */
    @Test
    public void testGetMinimalSeparators1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        assertEquals(0, separators.size());
        assertEquals(0, separatorsAndMultiplicities.size());
    }

    /**
     * Test on small chordal graph
     */
    @Test
    public void testGetMinimalSeparators2()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        Map<Set<Integer>, Integer> expected = new HashMap<>();
        expected.put(Set.of(2, 3), 1);
        assertEquals(expected.keySet(), separators);
        assertEquals(expected, separatorsAndMultiplicities);
    }

    /**
     * Test on big chordal graph (example from original article)
     */
    @Test
    public void testGetMinimalSeparators3()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 3, 6 }, { 3, 8 },
            { 3, 10 }, { 3, 11 }, { 4, 5 }, { 4, 6 }, { 5, 6 }, { 6, 7 }, { 6, 8 }, { 6, 10 },
            { 6, 11 }, { 7, 8 }, { 7, 10 }, { 8, 9 }, { 8, 10 }, { 9, 10 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        Map<Set<Integer>, Integer> expected = new HashMap<>();
        expected.put(Set.of(3), 1);
        expected.put(Set.of(3, 6), 2);
        expected.put(Set.of(8, 10), 1);
        expected.put(Set.of(6, 8, 10), 1);
        assertEquals(expected.keySet(), separators);
        assertEquals(expected, separatorsAndMultiplicities);
    }

    /**
     * Test on big chordal graph (example from original article)
     */
    @Test
    public void testGetMinimalSeparators4()
    {
        int[][] edges =
            { { 1, 2 }, { 2, 8 }, { 2, 9 }, { 3, 8 }, { 3, 9 }, { 4, 6 }, { 4, 8 }, { 5, 6 },
                { 5, 8 }, { 6, 7 }, { 6, 8 }, { 6, 9 }, { 7, 8 }, { 7, 9 }, { 8, 9 }, { 8, 10 },
                { 8, 11 }, { 8, 12 }, { 9, 10 }, { 9, 11 }, { 9, 12 }, { 10, 11 }, { 11, 12 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        Map<Set<Integer>, Integer> expected = new HashMap<>();
        expected.put(Set.of(2), 1);
        expected.put(Set.of(6, 8), 2);
        expected.put(Set.of(8, 9), 3);
        expected.put(Set.of(8, 9, 11), 1);
        assertEquals(expected.keySet(), separators);
        assertEquals(expected, separatorsAndMultiplicities);
    }

    /**
     * Test on not chordal graph
     */
    @Test
    public void testGetMinimalSeparators5()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        assertNull(separators);
        assertNull(separatorsAndMultiplicities);
    }

    /**
     * Test on pseudograph
     */
    @Test
    public void testGetMinimalSeparators6()
    {
        int[][] edges = { { 1, 1 }, { 1, 1 }, { 1, 2 }, { 2, 3 }, { 2, 3 }, { 2, 3 }, { 2, 5 },
            { 3, 3 }, { 3, 4 }, { 5, 3 }, { 5, 3 }, { 5, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalGraphMinimalVertexSeparatorFinder<Integer, DefaultEdge> finder =
            new ChordalGraphMinimalVertexSeparatorFinder<>(graph);
        Set<Set<Integer>> separators = finder.getMinimalSeparators();
        Map<Set<Integer>, Integer> separatorsAndMultiplicities =
            finder.getMinimalSeparatorsWithMultiplicities();
        Map<Set<Integer>, Integer> expected = new HashMap<>();
        expected.put(Set.of(2), 1);
        expected.put(Set.of(3, 5), 1);
        assertEquals(expected.keySet(), separators);
        assertEquals(expected, separatorsAndMultiplicities);
    }

}
