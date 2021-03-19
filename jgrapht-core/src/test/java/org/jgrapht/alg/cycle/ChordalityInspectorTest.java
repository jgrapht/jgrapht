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
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalityInspector}
 *
 * @author Timofey Chudakov
 */
@RunWith(Parameterized.class)
public class ChordalityInspectorTest
{
    private ChordalityInspector.IterationOrder iterationOrder;

    public ChordalityInspectorTest(ChordalityInspector.IterationOrder iterationOrder)
    {
        this.iterationOrder = iterationOrder;
    }

    @Parameterized.Parameters
    public static Object[] params()
    {
        return new Object[] { ChordalityInspector.IterationOrder.MCS,
            ChordalityInspector.IterationOrder.LEX_BFS };
    }

    /**
     * Tests usage of the correct iteration order
     */
    @Test
    public void testIterationOrder()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector =
            new ChordalityInspector<>(graph, iterationOrder);
        assertEquals(iterationOrder, inspector.getIterationOrder());
    }

    /**
     * Test on the big cycle
     */
    @Test
    public void testGetChordlessCycle()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        int upperBound = 100;
        for (int i = 0; i < upperBound; i++) {
            Graphs.addEdgeWithVertices(graph, i, i + 1);
        }
        Graphs.addEdgeWithVertices(graph, 0, upperBound);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
    }

    /**
     * Tests whether repeated calls to the {@link ChordalityInspector#getPerfectEliminationOrder()}
     * return the same vertex order.
     */
    @Test
    public void testPerfectEliminationOrder()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector =
            new ChordalityInspector<>(graph, iterationOrder);
        List<Integer> order1 = inspector.getPerfectEliminationOrder();
        assertNull(inspector.getHole());
        graph.removeVertex(1);
        List<Integer> order2 = inspector.getPerfectEliminationOrder();
        assertEquals(order1, order2);
        assertNull(inspector.getHole());
    }

    /**
     * Tests whether returned list is unmodifiable
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableList()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        List<Integer> perfectEliminationOrder = inspector.getPerfectEliminationOrder();
        perfectEliminationOrder.add(0);
    }

    /**
     * Test chordality inspection of an empty graph
     */
    @Test
    public void testIsChordal1()
    {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        List<Integer> perfectEliminationOrder = inspector.getPerfectEliminationOrder();
        assertNotNull(perfectEliminationOrder);
    }

    /**
     * Test on chordal graph with 4 vertices:<br>
     * 1--2 <br>
     * | \| <br>
     * 3--4 <br>
     */
    @Test
    public void testIsChordal2()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on chordal graph with two connected components: <br>
     * 1-2-3-1 and 4-5-6-4<br>
     */
    @Test
    public void testIsChordal3()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 }, { 5, 6 }, { 6, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on chordal connected graph with 10 vertices
     */
    @Test
    public void testIsChordal4()
    {
        int[][] edges =
            { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 5, 6 }, { 5, 7 },
                { 6, 7 }, { 7, 8 }, { 7, 9 }, { 8, 9 }, { 9, 1 }, { 10, 1 }, { 3, 7 }, { 1, 7 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on graph with 4-vertex cycle: 1-2-3-4-1
     */
    @Test
    public void testIsChordal5()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 1, 5 }, { 5, 2 }, { 2, 6 },
            { 6, 3 }, { 3, 7 }, { 7, 4 }, { 4, 8 }, { 8, 1 }, { 5, 6 }, { 6, 7 }, { 7, 8 },
            { 8, 5 }, { 5, 7 }, { 6, 8 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertFalse(inspector.isChordal());
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
        assertNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test on the chordal pseudograph
     */
    @Test
    public void testIsChordal6()
    {
        int[][] edges = { { 1, 1 }, { 1, 2 }, { 1, 2 }, { 1, 3 }, { 3, 1 }, { 2, 3 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createPseudograph(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertTrue(inspector.isChordal());
        assertNull(inspector.getHole());
        assertNotNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test of non-chordal pseudograph (cycle 2-3-4-5-2)
     */
    @Test
    public void testIsChordal7()
    {
        int[][] edges = { { 1, 1 }, { 1, 2 }, { 2, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 2, 3 },
            { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 2 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createPseudograph(edges);

        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph);
        assertFalse(inspector.isChordal());
        GraphPath<Integer, DefaultEdge> path = inspector.getHole();
        assertNotNull(path);
        assertIsHole(graph, path);
        assertNull(inspector.getPerfectEliminationOrder());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal8()
    {
        Graph<Integer, DefaultEdge> ellinghamHorton78 =
            NamedGraphGenerator.ellinghamHorton78Graph();
        ChordalityInspector<Integer, DefaultEdge> inspector =
            new ChordalityInspector<>(ellinghamHorton78);
        assertFalse(inspector.isChordal());
        assertIsHole(ellinghamHorton78, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal9()
    {
        Graph<Integer, DefaultEdge> gosset = NamedGraphGenerator.gossetGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(gosset);
        assertFalse(inspector.isChordal());
        assertIsHole(gosset, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal10()
    {
        Graph<Integer, DefaultEdge> klein = NamedGraphGenerator.klein3RegularGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(klein);
        assertFalse(inspector.isChordal());
        assertIsHole(klein, inspector.getHole());
    }

    /**
     * Test for correct hole detection
     */
    @Test
    public void testIsChordal11()
    {
        Graph<Integer, DefaultEdge> schläfli = NamedGraphGenerator.schläfliGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(schläfli);
        assertFalse(inspector.isChordal());
        assertIsHole(schläfli, inspector.getHole());
    }

    @Test
    public void testIsChordal12()
    {
        Graph<Integer, DefaultEdge> buckyBall = NamedGraphGenerator.buckyBallGraph();
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(buckyBall);
        assertFalse(inspector.isChordal());
        assertIsHole(buckyBall, inspector.getHole());
    }

    /**
     * Basic test for {@link ChordalityInspector#isPerfectEliminationOrder(List)}
     */
    @Test
    public void testIsPerfectEliminationOrder1()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 4 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        List<Integer> order = Arrays.asList(1, 2, 3, 4);
        assertFalse(
            new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * First test on 4-vertex cycle: 1-2-3-4-1 <br>
     * Second test with chord 2-4 added, so that the graph becomes chordal
     */
    @Test
    public void testIsPerfectEliminationOrder2()
    {
        int[][] edges = { { 1, 2 }, { 1, 4 }, { 2, 3 }, { 3, 4 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);
        List<Integer> order = Arrays.asList(1, 2, 4, 3);

        ChordalityInspector<Integer, DefaultEdge> inspector =
            new ChordalityInspector<>(graph, iterationOrder);
        assertFalse(
            "Not a perfect elimination order: cycle 1->2->3->4->1 has non chord",
            inspector.isPerfectEliminationOrder(order));
        graph.addEdge(2, 4);
        assertTrue(
            "Valid perfect elimination order: no induced cycles of length > 3",
            inspector.isPerfectEliminationOrder(order));
    }

    /**
     * Test on chordal graph:<br>
     * .......5<br>
     * ...../.|.\<br>
     * ....4--3--6--7<br>
     * ....|./.|.|\.|<br>
     * ....1--2..9--8<br>
     * ...........\.|<br>
     * ............10 <br>
     */
    @Test
    public void testIsPerfectEliminationOrder3()
    {
        int[][] edges =
            { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 3, 6 }, { 4, 5 },
                { 5, 6 }, { 6, 7 }, { 6, 8 }, { 6, 9 }, { 7, 8 }, { 8, 9 }, { 8, 10 }, { 9, 10 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertTrue(
            new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on big chordal graph with valid perfect elimination order
     */
    @Test
    public void testIsPerfectEliminationOrder4()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 3, 6 },
            { 3, 7 }, { 4, 5 }, { 5, 6 }, { 5, 7 }, { 6, 7 }, { 6, 8 }, { 7, 9 }, { 7, 10 },
            { 7, 11 }, { 9, 10 }, { 9, 11 }, { 9, 12 }, { 10, 11 }, { 11, 12 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        assertTrue(
            "Valid perfect elimination order",
            new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on chordal graph with invalid perfect elimination order
     */
    @Test
    public void testIsPerfectEliminationOrder5()
    {
        int[][] edges = { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, { 3, 5 }, { 4, 5 },
            { 4, 6 }, { 5, 6 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        List<Integer> order = Arrays.asList(1, 2, 5, 6, 4, 3);
        assertFalse(
            "Graph is chordal, order isn't perfect elimination order",
            new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on graph with 5-vertex cycle 2-4-6-8-10-2 with no chords
     */
    @Test
    public void testIsPerfectEliminationOrder6()
    {
        int[][] edges = { { 1, 2 }, { 2, 3 }, { 2, 4 }, { 3, 4 }, { 4, 5 }, { 4, 6 }, { 5, 6 },
            { 6, 7 }, { 6, 8 }, { 7, 8 }, { 8, 9 }, { 8, 10 }, { 9, 10 }, { 10, 1 }, { 10, 2 }, };
        Graph<Integer, DefaultEdge> graph = TestUtil.createUndirected(edges);

        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertFalse(
            "Cycle 2->4->6->8->10->2 has no chords => no perfect elimination order",
            new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Checks whether {@code cycle} is a hole in {@code graph}
     *
     * @param graph the tested graph.
     * @param path the tested cycle.
     * @param <V> graph vertex type.
     * @param <E> graph edge type.
     */
    private <V, E> void assertIsHole(Graph<V, E> graph, GraphPath<V, E> path)
    {
        List<V> cycle = path.getVertexList();
        assertTrue(cycle.size() > 4);
        for (int i = 0; i < cycle.size() - 1; i++) {
            assertTrue(graph.containsEdge(cycle.get(i), cycle.get(i + 1)));
        }
        for (int i = 0; i < cycle.size() - 2; i++) {
            for (int j = 0; j < cycle.size() - 2; j++) {
                if (Math.abs(i - j) > 1) {
                    assertFalse(graph.containsEdge(cycle.get(i), cycle.get(j)));
                }
            }
        }
    }
}
