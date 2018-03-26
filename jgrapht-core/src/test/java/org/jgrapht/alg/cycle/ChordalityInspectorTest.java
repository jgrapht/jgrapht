/*
 * (C) Copyright 2018, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ChordalityInspector}
 *
 * @author Timofey Chudakov
 */
@RunWith(Parameterized.class)
public class ChordalityInspectorTest {
    private ChordalityInspector.IterationOrder iterationOrder;

    public ChordalityInspectorTest(ChordalityInspector.IterationOrder iterationOrder) {
        this.iterationOrder = iterationOrder;
    }

    @Parameterized.Parameters
    public static Object[] params() {
        return new Object[]{ChordalityInspector.IterationOrder.MCS, ChordalityInspector.IterationOrder.LEX_BFS};
    }

    /**
     * Tests usage of the correct iteration order
     */
    @Test
    public void testIterationOrder() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        assertEquals(inspector.getIterationOrder(), iterationOrder);
    }

    /**
     * Tests whether repeated calls to the {@link ChordalityInspector#getSearchOrder()} return
     * the same vertex order.
     */
    @Test
    public void testGetOrder() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        List<Integer> order1 = inspector.getSearchOrder();
        graph.removeVertex(1);
        List<Integer> order2 = inspector.getSearchOrder();
        assertEquals(order1, order2);
    }

    /**
     * Test on chordal graph with 4 vertices:<br>
     * 1--2 <br>
     * | \| <br>
     * 3--4 <br>
     */
    @Test
    public void testChordalGraphRecognition1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Test on chordal graph with two connected components: <br>
     * 1-2-3-1  and 4-5-6-4<br>
     */
    @Test
    public void testChordalGraphRecognition2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Test on chordal connected graph with 10 vertices
     */
    @Test
    public void testChordalGraphRecognition3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addVertex(7);
        graph.addVertex(8);
        graph.addVertex(9);
        graph.addVertex(10);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(5, 7);
        graph.addEdge(6, 7);
        graph.addEdge(7, 8);
        graph.addEdge(7, 9);
        graph.addEdge(8, 9);
        graph.addEdge(9, 1);
        graph.addEdge(9, 10);
        graph.addEdge(10, 1);
        graph.addEdge(3, 7);
        graph.addEdge(1, 7);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Test on graph with 4-vertex cycle: 1-2-3-4-1
     */
    @Test
    public void testChordalGraphRecognition4() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addVertex(7);
        graph.addVertex(8);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 1);
        graph.addEdge(1, 5);
        graph.addEdge(5, 2);
        graph.addEdge(2, 6);
        graph.addEdge(6, 3);
        graph.addEdge(3, 7);
        graph.addEdge(7, 4);
        graph.addEdge(4, 8);
        graph.addEdge(8, 1);
        graph.addEdge(5, 6);
        graph.addEdge(6, 7);
        graph.addEdge(7, 8);
        graph.addEdge(8, 5);
        graph.addEdge(5, 7);
        graph.addEdge(6, 8);
        assertFalse(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Test on the chordal pseudograph
     */
    @Test
    public void testChordalGraphRecognition5() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addEdge(1, 1);
        graph.addEdge(1, 2);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(3, 1);
        graph.addEdge(2, 3);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Test of non-chordal pseudograph (cycle 2-3-4-5-2)
     */
    @Test
    public void testChordalGraphRecognition6() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addEdge(1, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 1);
        graph.addEdge(2, 2);
        graph.addEdge(3, 3);
        graph.addEdge(4, 4);
        graph.addEdge(2, 3);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 2);
        assertFalse(new ChordalityInspector<>(graph, iterationOrder).isChordal());
    }

    /**
     * Basic test for {@link ChordalityInspector#isPerfectEliminationOrder(List)}
     */
    @Test
    public void testPerfectEliminationOrderRecognition1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4);
        for (Integer v : order) {
            graph.addVertex(v);
        }
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        assertFalse(new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * First test on 4-vertex cycle: 1-2-3-4-1 <br>
     * Second test with chord 2-4 added, so that the graph becomes chordal
     */
    @Test
    public void testPerfectEliminationOrderRecognition2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 4, 3);
        for (Integer v : order) {
            graph.addVertex(v);
        }
        graph.addEdge(1, 2);
        graph.addEdge(1, 4);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        ChordalityInspector<Integer, DefaultEdge> inspector = new ChordalityInspector<>(graph, iterationOrder);
        assertFalse("Not a perfect elimination order: cycle 1->2->3->4->1 has non chord", inspector.isPerfectEliminationOrder(order));
        graph.addEdge(2, 4);
        assertTrue("Valid perfect elimination order: no induced cycles of length > 3", inspector.isPerfectEliminationOrder(order));
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
    public void testPerfectEliminationOrderRecognition3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (Integer v : order) {
            graph.addVertex(v);
        }
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(3, 6);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 7);
        graph.addEdge(6, 8);
        graph.addEdge(6, 9);
        graph.addEdge(7, 8);
        graph.addEdge(8, 9);
        graph.addEdge(8, 10);
        graph.addEdge(9, 10);
        assertTrue(new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on big chordal graph with valid perfect elimination order
     */
    @Test
    public void testPerfectEliminationOrderRecognition4() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        for (Integer v : order) {
            graph.addVertex(v);
        }
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(3, 6);
        graph.addEdge(3, 7);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(5, 7);
        graph.addEdge(6, 7);
        graph.addEdge(6, 8);
        graph.addEdge(7, 9);
        graph.addEdge(7, 10);
        graph.addEdge(7, 11);
        graph.addEdge(9, 10);
        graph.addEdge(9, 11);
        graph.addEdge(9, 12);
        graph.addEdge(10, 11);
        graph.addEdge(11, 12);
        assertTrue("Valid perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on chordal graph with invalid perfect elimination order
     */
    @Test
    public void testPerfectEliminationOrderRecognition5() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 5, 6, 4, 3);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.addVertex(6);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);
        graph.addEdge(4, 6);
        graph.addEdge(5, 6);
        assertFalse("Graph is chordal, order isn't perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

    /**
     * Test on graph with 5-vertex cycle 2-4-6-8-10-2 with no chords
     */
    @Test
    public void testPerfectEliminationOrderRecognition6() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        List<Integer> order = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (Integer v : order) {
            graph.addVertex(v);
        }
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);
        graph.addEdge(4, 6);
        graph.addEdge(5, 6);
        graph.addEdge(6, 7);
        graph.addEdge(6, 8);
        graph.addEdge(7, 8);
        graph.addEdge(8, 9);
        graph.addEdge(8, 10);
        graph.addEdge(9, 10);
        graph.addEdge(10, 1);
        graph.addEdge(10, 2);
        assertFalse("Cycle 2->4->6->8->10->2 has no chords => no perfect elimination order",
                new ChordalityInspector<>(graph, iterationOrder).isPerfectEliminationOrder(order));
    }

}

